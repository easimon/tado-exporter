package click.dobel.tado.exporter.apiclient.logging

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import mu.KotlinLogging
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.stream.Collectors

fun RestTemplateBuilder.loggingIf(boolean: Boolean): RestTemplateBuilder = if (boolean) logging() else this
fun RestTemplateBuilder.loggingIfConfigured(configuration: TadoConfigurationProperties): RestTemplateBuilder =
  loggingIf(configuration.debug.http)

private class LoggingHttpRequestInterceptor : ClientHttpRequestInterceptor {
  companion object {
    var logger = KotlinLogging.logger {}
  }

  override fun intercept(
    req: HttpRequest, reqBody: ByteArray, execution: ClientHttpRequestExecution
  ): ClientHttpResponse {
    logger.trace { "${req.method} ${req.uri}" }
    logger.trace { "Request  headers: ${req.headers.loggable()}" }
    logger.trace { "Request  body   : ${String(reqBody, Charsets.UTF_8).orNone()} " }

    try {
      val response: ClientHttpResponse = execution.execute(req, reqBody)

      logger.trace { "Response headers: ${response.headers}" }
      response.body.use { bodyStream ->
        val isr = InputStreamReader(bodyStream, Charsets.UTF_8)
        val body = BufferedReader(isr).lines()
          .collect(Collectors.joining("\n"))
        logger.trace { "Response body   : ${body.orNone()}" }
      }
      return response
    } catch (e: IOException) {
      logger.warn(e) { "Error occurred while executing request." }
      throw e
    }
  }

  private fun String.orNone() = ifEmpty { "<none>" }
}

private fun ClientHttpRequestFactory.buffering(): ClientHttpRequestFactory =
  if (this is BufferingClientHttpRequestFactory) this
  else BufferingClientHttpRequestFactory(this)

private fun RestTemplateBuilder.buffering(): RestTemplateBuilder = requestFactory(this.buildRequestFactory()::buffering)
private fun RestTemplateBuilder.logging(): RestTemplateBuilder =
  buffering().interceptors(LoggingHttpRequestInterceptor())

private fun HttpHeaders.loggable(): Map<String, List<String>> =
  mapValues { (header, values) ->
    when (header) {
      HttpHeaders.AUTHORIZATION -> values.censorAuthorization()
      else -> values
    }
  }

private fun Collection<String>.censorAuthorization() = map {
  val method = it.substringBefore(" ", "***")
  val censoredValue = "***"
  "$method $censoredValue"
}
