package click.dobel.tado.client.auth

import click.dobel.tado.client.auth.request.TadoAuthLoginRequest
import click.dobel.tado.client.auth.request.TadoAuthRefreshRequest
import click.dobel.tado.client.auth.request.TadoAuthRequest
import click.dobel.tado.client.auth.response.TadoAuthResponse
import click.dobel.tado.test.AuthMockMappings
import click.dobel.tado.test.TestConfiguration
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.filter.ClientFilterChain
import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import jakarta.inject.Provider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

internal class TadoAuthFilterTest : StringSpec({

  val tadoConfiguration = TestConfiguration.INSTANCE
  lateinit var authClient: AuthClient
  lateinit var authClientProvider: Provider<AuthClient>

  lateinit var filter: TadoAuthFilter

  beforeTest {
    authClient = mockk()
    authClientProvider = Provider { authClient }
    filter = TadoAuthFilter(
      authClientProvider,
      tadoConfiguration
    )
  }

  "retrieves access token by username / password and injects it into the request" {
    every {
      authClient.token(any())
    } returns Mono.just(
      TadoAuthResponse(
        AuthMockMappings.DEFAULT_ACCESS_TOKEN,
        AuthMockMappings.DEFAULT_TOKEN_TYPE,
        AuthMockMappings.DEFAULT_REFRESH_TOKEN,
        AuthMockMappings.DEFAULT_EXPIRES_IN,
        AuthMockMappings.DEFAULT_SCOPE,
        AuthMockMappings.DEFAULT_JTI
      )
    )

    val request = mockk<MutableHttpRequest<TadoAuthRequest>>()
    val chain = mockk<ClientFilterChain>()
    val response = mockk<HttpResponse<*>>()

    every { chain.proceed(any()) } returns Mono.just(response)
    every { request.bearerAuth(any()) } returns request

    withContext(Dispatchers.IO) {
      filter.doFilter(request, chain).toMono().block()
    }

    verifySequence {
      authClient.token(TadoAuthLoginRequest(tadoConfiguration))
      request.bearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
      chain.proceed(request)
      // no checks on publisher
    }
  }

  "on second call, retrieves access token by refresh token and injects it into the request" {
    every {
      authClient.token(any())
    } returns Mono.just(
      TadoAuthResponse(
        AuthMockMappings.DEFAULT_ACCESS_TOKEN,
        AuthMockMappings.DEFAULT_TOKEN_TYPE,
        AuthMockMappings.DEFAULT_REFRESH_TOKEN,
        -1, // expire immediately
        AuthMockMappings.DEFAULT_SCOPE,
        AuthMockMappings.DEFAULT_JTI
      )
    )

    val request = mockk<MutableHttpRequest<TadoAuthRequest>>()
    val chain = mockk<ClientFilterChain>()
    val response = mockk<HttpResponse<*>>()

    every { chain.proceed(any()) } returns Mono.just(response)
    every { request.bearerAuth(any()) } returns request

    withContext(Dispatchers.IO) {
      filter.doFilter(request, chain).toMono().block()
      filter.doFilter(request, chain).toMono().block()
    }

    verifySequence {
      authClient.token(TadoAuthLoginRequest(tadoConfiguration))
      request.bearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
      chain.proceed(request)

      authClient.token(TadoAuthRefreshRequest(tadoConfiguration, AuthMockMappings.DEFAULT_REFRESH_TOKEN))
      request.bearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
      chain.proceed(request)
    }
  }

  "retries authentication with username / password when refresh token is rejected" {
    every {
      authClient.token(ofType<TadoAuthLoginRequest>())
    } returns Mono.just(
      TadoAuthResponse(
        AuthMockMappings.DEFAULT_ACCESS_TOKEN,
        AuthMockMappings.DEFAULT_TOKEN_TYPE,
        AuthMockMappings.DEFAULT_REFRESH_TOKEN,
        -1, // expire immediately
        AuthMockMappings.DEFAULT_SCOPE,
        AuthMockMappings.DEFAULT_JTI
      )
    )

    every {
      authClient.token(ofType<TadoAuthRefreshRequest>())
    } returns Mono.error(HttpClientResponseException("TestException", mockk(relaxed = true)))

    val request = mockk<MutableHttpRequest<TadoAuthRequest>>()
    val chain = mockk<ClientFilterChain>()
    val response = mockk<HttpResponse<*>>()

    every { chain.proceed(any()) } returns Mono.just(response)
    every { request.bearerAuth(any()) } returns request

    withContext(Dispatchers.IO) {
      filter.doFilter(request, chain).toMono().block()
      filter.doFilter(request, chain).toMono().block()
    }

    verifySequence {
      authClient.token(TadoAuthLoginRequest(tadoConfiguration))
      request.bearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
      chain.proceed(request)

      authClient.token(TadoAuthRefreshRequest(tadoConfiguration, AuthMockMappings.DEFAULT_REFRESH_TOKEN))
      authClient.token(TadoAuthLoginRequest(tadoConfiguration))
      request.bearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
      chain.proceed(request)
    }
  }

  "passes on HttpClientResponseException" {
    every {
      authClient.token(any())
    } returns Mono.error(HttpClientResponseException("TestException", mockk(relaxed = true)))

    val request = mockk<MutableHttpRequest<TadoAuthRequest>>()
    val chain = mockk<ClientFilterChain>()
    val response = mockk<HttpResponse<*>>()

    every { chain.proceed(any()) } returns Mono.just(response)
    every { request.bearerAuth(any()) } returns request

    shouldThrow<HttpClientResponseException> {
      withContext(Dispatchers.IO) {
        filter.doFilter(request, chain).toMono().block()
      }
    }

    verifySequence {
      authClient.token(TadoAuthLoginRequest(tadoConfiguration))
      request wasNot called
      chain wasNot called
    }
  }
})
