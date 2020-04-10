package click.dobel.tado.client.auth

import click.dobel.tado.test.AuthMockMappings
import click.dobel.tado.test.TestConfiguration
import io.kotlintest.TestCase
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.filter.ClientFilterChain
import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import org.reactivestreams.Publisher

internal class TadoAuthFilterTest : StringSpec() {
  val tadoConfiguration = TestConfiguration.INSTANCE
  lateinit var authClient: AuthClient
  lateinit var filter: TadoAuthFilter

  init {
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

      val request = mockk<MutableHttpRequest<TadoAuthRequest>>()
      val chain = mockk<ClientFilterChain>()
      val publisher = mockk<Publisher<HttpResponse<*>>>()

      every { chain.proceed(any()) } returns publisher
      every { request.bearerAuth(any()) } returns request

      filter.doFilter(request, chain) shouldBeSameInstanceAs publisher

      verifySequence {
        authClient.token(TadoAuthRequest.TadoAuthLoginRequest(tadoConfiguration))
        request.bearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
        chain.proceed(request)
        // no checks on publisher
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

      val request = mockk<MutableHttpRequest<TadoAuthRequest>>()
      val chain = mockk<ClientFilterChain>()
      val publisher = mockk<Publisher<HttpResponse<*>>>()

      every { chain.proceed(any()) } returns publisher
      every { request.bearerAuth(any()) } returns request

      filter.doFilter(request, chain) shouldBeSameInstanceAs publisher
      filter.doFilter(request, chain) shouldBeSameInstanceAs publisher

      verifySequence {
        authClient.token(TadoAuthRequest.TadoAuthLoginRequest(tadoConfiguration))
        request.bearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
        chain.proceed(request)
        authClient.token(TadoAuthRequest.TadoAuthRefreshRequest(tadoConfiguration, AuthMockMappings.DEFAULT_REFRESH_TOKEN))
        request.bearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
        chain.proceed(request)
        // no checks on publisher
      }
    }

    "retries authentication with username / password when refresh token is rejected" {
      every {
        authClient.token(ofType<TadoAuthRequest.TadoAuthLoginRequest>())
      } returns TadoAuthResponse(
        AuthMockMappings.DEFAULT_ACCESS_TOKEN,
        AuthMockMappings.DEFAULT_TOKEN_TYPE,
        AuthMockMappings.DEFAULT_REFRESH_TOKEN,
        -1, // expire immediately
        AuthMockMappings.DEFAULT_SCOPE,
        AuthMockMappings.DEFAULT_JTI
      )

      every {
        authClient.token(ofType<TadoAuthRequest.TadoAuthRefreshRequest>())
      } throws HttpClientResponseException("TestException", mockk(relaxed = true))

      val request = mockk<MutableHttpRequest<TadoAuthRequest>>()
      val chain = mockk<ClientFilterChain>()
      val publisher = mockk<Publisher<HttpResponse<*>>>()

      every { chain.proceed(any()) } returns publisher
      every { request.bearerAuth(any()) } returns request

      filter.doFilter(request, chain) shouldBeSameInstanceAs publisher
      filter.doFilter(request, chain) shouldBeSameInstanceAs publisher

      verifySequence {
        authClient.token(TadoAuthRequest.TadoAuthLoginRequest(tadoConfiguration))
        request.bearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
        chain.proceed(request)
        authClient.token(TadoAuthRequest.TadoAuthRefreshRequest(tadoConfiguration, AuthMockMappings.DEFAULT_REFRESH_TOKEN))
        authClient.token(TadoAuthRequest.TadoAuthLoginRequest(tadoConfiguration))
        request.bearerAuth(AuthMockMappings.DEFAULT_ACCESS_TOKEN)
        chain.proceed(request)
        // no checks on publisher, mockException
      }
    }

    "rethrows HttpClientResponseException" {
      every {
        authClient.token(any())
      } throws HttpClientResponseException("TestException", mockk(relaxed = true))

      val request = mockk<MutableHttpRequest<TadoAuthRequest>>()
      val chain = mockk<ClientFilterChain>()
      val publisher = mockk<Publisher<HttpResponse<*>>>()

      every { chain.proceed(any()) } returns publisher
      every { request.bearerAuth(any()) } returns request

      shouldThrow<HttpClientResponseException> {
        filter.doFilter(request, chain)
      }

      verifySequence {
        authClient.token(TadoAuthRequest.TadoAuthLoginRequest(tadoConfiguration))
        request wasNot called
        chain wasNot called
        // no checks on publisher
      }
    }
  }

  override fun beforeTest(testCase: TestCase) {
    authClient = mockk()
    filter = TadoAuthFilter(
      authClient,
      tadoConfiguration
    )
  }
}
