package click.dobel.tado.exporter.apiclient.auth

/*
internal class TadoAuthFilterTest : StringSpec({

  val tadoConfiguration = TestConfiguration.INSTANCE
  lateinit var tadoAuthenticationClient: TadoAuthenticationClient

  lateinit var filter: TadoAuthFilter

  val body = byteArrayOf()

  beforeTest {
    tadoAuthenticationClient = mockk()
    filter = TadoAuthFilter(
      tadoConfiguration,
      tadoAuthenticationClient
    )
  }

  "retrieves access token by username / password and injects it into the request" {
    every {
      tadoAuthenticationClient.token(any())
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
      tadoAuthenticationClient.token(TadoAuthTokenRequest(tadoConfiguration))
      headers.setBearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
      headers.accept = listOf(MediaType.APPLICATION_JSON)
      execution.execute(request, body)
    }
  }

  "on second call, retrieves access token by refresh token and injects it into the request" {
    every {
      tadoAuthenticationClient.token(any())
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
      tadoAuthenticationClient.token(TadoAuthTokenRequest(tadoConfiguration))
      headers.setBearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
      headers.accept = listOf(MediaType.APPLICATION_JSON)
      execution.execute(request, body)

      tadoAuthenticationClient.token(TadoAuthRefreshRequest(tadoConfiguration, AuthMockMappings.DEFAULT_REFRESH_TOKEN))
      headers.setBearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
      headers.accept = listOf(MediaType.APPLICATION_JSON)
      execution.execute(request, body)
    }
  }

  "retries authentication with username / password when refresh token is rejected" {
    every {
      tadoAuthenticationClient.token(ofType<TadoAuthTokenRequest>())
    } returns TadoAuthResponse(
      AuthMockMappings.DEFAULT_ACCESS_TOKEN,
      AuthMockMappings.DEFAULT_TOKEN_TYPE,
      AuthMockMappings.DEFAULT_REFRESH_TOKEN,
      -1, // expire immediately
      AuthMockMappings.DEFAULT_SCOPE,
      AuthMockMappings.DEFAULT_JTI
    )

    every {
      tadoAuthenticationClient.token(ofType<TadoAuthRefreshRequest>())
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
      tadoAuthenticationClient.token(TadoAuthTokenRequest(tadoConfiguration))
      headers.setBearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
      headers.accept = listOf(MediaType.APPLICATION_JSON)
      execution.execute(request, body)

      tadoAuthenticationClient.token(TadoAuthRefreshRequest(tadoConfiguration, AuthMockMappings.DEFAULT_REFRESH_TOKEN))
      tadoAuthenticationClient.token(TadoAuthTokenRequest(tadoConfiguration))
      headers.setBearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
      headers.accept = listOf(MediaType.APPLICATION_JSON)
      execution.execute(request, body)
    }
  }

  "passes on HttpClientResponseException" {
    every {
      tadoAuthenticationClient.token(any())
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
      tadoAuthenticationClient.token(TadoAuthTokenRequest(tadoConfiguration))
      headers wasNot called
      execution wasNot called
    }
  }
})
*/
