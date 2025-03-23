package click.dobel.tado.exporter.apiclient

import click.dobel.tado.api.HomeInfo
import click.dobel.tado.api.HomeState
import click.dobel.tado.api.User
import click.dobel.tado.api.WeatherReport
import click.dobel.tado.api.ZoneState
import click.dobel.tado.exporter.apiclient.auth.TadoAuthenticator
import click.dobel.tado.exporter.apiclient.auth.model.accesstoken.AccessToken
import click.dobel.tado.exporter.apiclient.logging.loggingIfConfigured
import click.dobel.tado.exporter.apiclient.model.Zones
import mu.KotlinLogging
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient

@Component
class TadoApiClient(
  private val configuration: TadoConfigurationProperties,
  private val authenticationState: TadoAuthenticator,
  restTemplateBuilder: RestTemplateBuilder
) {

  companion object {
    private val logger = KotlinLogging.logger {}
    const val BASE_URL = "/api/v2"

    const val ME_PATH = "/me"
    const val HOMES_PATH = "/homes"
    const val ZONES_PATH = "/zones"
    const val STATE_PATH = "/state"
    const val WEATHER_PATH = "/weather"
  }

  val isAuthenticated: Boolean get() = authenticationState.accessToken is AccessToken.BearerToken

  @Cacheable("User", sync = true)
  fun me() = get<User>(ME_PATH)

  @Cacheable("HomeInfo", sync = true)
  fun homes(homeId: Int): HomeInfo =
    get("$HOMES_PATH/${homeId}")

  @Cacheable("HomeState", sync = true)
  fun homeState(homeId: Int): HomeState =
    get("$HOMES_PATH/${homeId}/state")

  @Cacheable("Zone", sync = true)
  fun zones(homeId: Int): Zones =
    get("$HOMES_PATH/${homeId}/$ZONES_PATH")

  @Cacheable("ZoneState", sync = true)
  fun zoneState(homeId: Int, zoneId: Int): ZoneState = logger
    .info { "Retrieving fresh zone state for HomeId $homeId, ZoneId $zoneId." }
    .run { get("$HOMES_PATH/${homeId}/$ZONES_PATH/${zoneId}/$STATE_PATH") }

  @Cacheable("WeatherReport", sync = true)
  fun weather(homeId: Int): WeatherReport = logger
    .info { "Retrieving fresh weather report for HomeId $homeId." }
    .run { get("$HOMES_PATH/${homeId}/$WEATHER_PATH") }

  private inline fun <reified T : Any> get(
    path: String
  ): T = when (val token = authenticationState.accessToken) {
    is AccessToken.BearerToken -> {
      restClient.get()
        .uri(url(path))
        .bearerAuth(token)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyOrError()
    }

    else -> {
      throw ResourceAccessException(
        "Skipping request, not authenticated (${authenticationState.currentStateAsString})"
      )
    }
  }

  private fun url(
    path: String
  ): String = "${configuration.apiServer}$BASE_URL${slash(path)}"

  private fun slash(
    path: String
  ): String = if (path.startsWith("/")) path else "/$path"

  private val restClient: RestClient = RestClient.create(
    restTemplateBuilder
      .loggingIfConfigured(configuration)
      .userAgent()
      .build()
  )
}
