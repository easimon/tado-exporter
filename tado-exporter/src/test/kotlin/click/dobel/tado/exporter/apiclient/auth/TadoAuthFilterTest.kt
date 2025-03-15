package click.dobel.tado.exporter.apiclient.auth

import click.dobel.tado.exporter.apiclient.auth.model.request.TadoAuthLoginRequest
import click.dobel.tado.exporter.apiclient.auth.model.request.TadoAuthRefreshRequest
import click.dobel.tado.exporter.apiclient.auth.model.response.TadoAuthResponse
import click.dobel.tado.exporter.test.AuthMockMappings
import click.dobel.tado.exporter.test.TestConfiguration
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.mockk.called
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verifySequence
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.RestClientException

internal class TadoAuthFilterTest : StringSpec({

  val tadoConfiguration = TestConfiguration.INSTANCE
  lateinit var authClient: AuthClient

  lateinit var filter: TadoAuthFilter

  val body = byteArrayOf()

  beforeTest {
    authClient = mockk()
    filter = TadoAuthFilter(
      tadoConfiguration,
      authClient
    )
  }

  "retrieves access token by username / password and injects it into the request" {
    every {
      authClient.token(any())
    } returns TadoAuthResponse(
      AuthMockMappings.DEFAULT_ACCESS_TOKEN,
      AuthMockMappings.DEFAULT_TOKEN_TYPE,
      AuthMockMappings.DEFAULT_REFRESH_TOKEN,
      AuthMockMappings.DEFAULT_EXPIRES_IN,
      AuthMockMappings.DEFAULT_SCOPE,
      AuthMockMappings.DEFAULT_JTI
    )

    val request = mockk<HttpRequest>()
    val headers = mockk<HttpHeaders>()
    val execution = mockk<ClientHttpRequestExecution>()
    val response = mockk<ClientHttpResponse>()

    every { request.headers } returns headers
    every { headers.setBearerAuth(any()) } just runs
    every { headers.accept = any() } just runs
    every { execution.execute(any(), any()) } returns response

    filter.intercept(request, body, execution)

    verifySequence {
      authClient.token(TadoAuthLoginRequest(tadoConfiguration))
      headers.setBearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
      headers.accept = listOf(MediaType.APPLICATION_JSON)
      execution.execute(request, body)
    }
  }

  "on second call, retrieves access token by refresh token and injects it into the request" {
    every {
      authClient.token(any())
    } returns TadoAuthResponse(
      AuthMockMappings.DEFAULT_ACCESS_TOKEN,
      AuthMockMappings.DEFAULT_TOKEN_TYPE,
      AuthMockMappings.DEFAULT_REFRESH_TOKEN,
      -1, // expire immediately
      AuthMockMappings.DEFAULT_SCOPE,
      AuthMockMappings.DEFAULT_JTI
    )

    val request = mockk<HttpRequest>()
    val headers = mockk<HttpHeaders>()
    val execution = mockk<ClientHttpRequestExecution>()
    val response = mockk<ClientHttpResponse>()

    every { request.headers } returns headers
    every { headers.setBearerAuth(any()) } just runs
    every { headers.accept = any() } just runs
    every { execution.execute(any(), any()) } returns response

    filter.intercept(request, body, execution)
    filter.intercept(request, body, execution)

    verifySequence {
      authClient.token(TadoAuthLoginRequest(tadoConfiguration))
      headers.setBearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
      headers.accept = listOf(MediaType.APPLICATION_JSON)
      execution.execute(request, body)

      authClient.token(TadoAuthRefreshRequest(tadoConfiguration, AuthMockMappings.DEFAULT_REFRESH_TOKEN))
      headers.setBearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
      headers.accept = listOf(MediaType.APPLICATION_JSON)
      execution.execute(request, body)
    }
  }

  "retries authentication with username / password when refresh token is rejected" {
    every {
      authClient.token(ofType<TadoAuthLoginRequest>())
    } returns TadoAuthResponse(
      AuthMockMappings.DEFAULT_ACCESS_TOKEN,
      AuthMockMappings.DEFAULT_TOKEN_TYPE,
      AuthMockMappings.DEFAULT_REFRESH_TOKEN,
      -1, // expire immediately
      AuthMockMappings.DEFAULT_SCOPE,
      AuthMockMappings.DEFAULT_JTI
    )

    every {
      authClient.token(ofType<TadoAuthRefreshRequest>())
    } throws RestClientException("TestException")

    val request = mockk<HttpRequest>()
    val headers = mockk<HttpHeaders>()
    val execution = mockk<ClientHttpRequestExecution>()
    val response = mockk<ClientHttpResponse>()

    every { request.headers } returns headers
    every { headers.setBearerAuth(any()) } just runs
    every { headers.accept = any() } just runs
    every { execution.execute(any(), any()) } returns response

    filter.intercept(request, body, execution)
    filter.intercept(request, body, execution)

    verifySequence {
      authClient.token(TadoAuthLoginRequest(tadoConfiguration))
      headers.setBearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
      headers.accept = listOf(MediaType.APPLICATION_JSON)
      execution.execute(request, body)

      authClient.token(TadoAuthRefreshRequest(tadoConfiguration, AuthMockMappings.DEFAULT_REFRESH_TOKEN))
      authClient.token(TadoAuthLoginRequest(tadoConfiguration))
      headers.setBearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
      headers.accept = listOf(MediaType.APPLICATION_JSON)
      execution.execute(request, body)
    }
  }

  "passes on HttpClientResponseException" {
    every {
      authClient.token(any())
    } throws RestClientException("TestException")

    val request = mockk<HttpRequest>()
    val headers = mockk<HttpHeaders>()
    val execution = mockk<ClientHttpRequestExecution>()
    val response = mockk<ClientHttpResponse>()

    every { request.headers } returns headers
    every { headers.setBearerAuth(any()) } just runs
    every { headers.accept = any() } just runs
    every { execution.execute(any(), any()) } returns response

    shouldThrow<RestClientException> {
      filter.intercept(request, body, execution)
    }

    verifySequence {
      authClient.token(TadoAuthLoginRequest(tadoConfiguration))
      headers wasNot called
      execution wasNot called
    }
  }
})
