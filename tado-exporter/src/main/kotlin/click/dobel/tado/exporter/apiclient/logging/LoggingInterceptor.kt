package click.dobel.tado.exporter.apiclient.logging

import click.dobel.tado.exporter.apiclient.TadoConfigurationProperties
import click.dobel.tado.util.logger
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpRequest
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors

fun RestTemplateBuilder.loggingIf(boolean: Boolean): RestTemplateBuilder = if (boolean) logging() else this
fun RestTemplateBuilder.loggingIfConfigured(configuration: TadoConfigurationProperties): RestTemplateBuilder =
  loggingIf(configuration.debug.http)

private class LoggingInterceptor : ClientHttpRequestInterceptor {
  override fun intercept(
    req: HttpRequest, reqBody: ByteArray, ex: ClientHttpRequestExecution
  ): ClientHttpResponse {
    LOGGER.trace("Request headers: {}", req.headers)
    LOGGER.trace("Request body: {}", String(reqBody, Charsets.UTF_8))

    val response: ClientHttpResponse = ex.execute(req, reqBody)

    LOGGER.trace("Response headers: {}", response.headers)
    val isr = InputStreamReader(response.body, Charsets.UTF_8)
    val body = BufferedReader(isr).lines()
      .collect(Collectors.joining("\n"))
    LOGGER.trace("Response body: {}", body)
    return response
  }

  companion object {
    var LOGGER = logger()
  }
}

private fun ClientHttpRequestFactory.buffering(): ClientHttpRequestFactory =
  if (this is BufferingClientHttpRequestFactory) this
  else BufferingClientHttpRequestFactory(this)

private fun RestTemplateBuilder.buffering(): RestTemplateBuilder = requestFactory(this.buildRequestFactory()::buffering)
private fun RestTemplateBuilder.logging(): RestTemplateBuilder = buffering().interceptors(LoggingInterceptor())
