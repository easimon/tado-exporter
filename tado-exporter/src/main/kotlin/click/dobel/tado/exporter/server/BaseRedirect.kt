package click.dobel.tado.exporter.server

import click.dobel.tado.exporter.apiclient.auth.TadoAuthenticationState
import click.dobel.tado.exporter.apiclient.auth.TadoAuthenticator
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class BaseRedirect(
  private val tadoAuthenticationState: TadoAuthenticator
) {
  companion object {
    internal const val PATH_ROOT = "/"
  }

  @GetMapping(PATH_ROOT)
  fun root(): ResponseEntity<String> {
    return ResponseEntity
      .status(HttpStatus.OK)
      .contentType(MediaType.TEXT_HTML)
      .body(home())
  }

  // TODO: does the job, but hey...
  private fun home() = """
      <html>
      <head>
        <title>tado-exporter</title>
      </head>
      <body>
        ${getAuthenticationRow()}
        <p><a href="/prometheus">Metrics</a></p>
      </body>
      </html>
    """.trimIndent()

  fun getAuthenticationRow(): String {
    val state = tadoAuthenticationState.currentState
    val result = if (state is TadoAuthenticationState.PollingForDeviceCodeToken) {
      """
        <p>Authentication state: Pending. <a href="${state.deviceAuthResponse.verificationUriComplete}">click here</a> to complete</p>
      """.trimIndent()
    } else {
      """
        <p>Authentication state: ${state.description}</p>
      """.trimIndent()
    }
    return result
  }
}
