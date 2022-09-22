package click.dobel.tado.exporter.server

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class BaseRedirect {
  companion object {
    internal const val PATH_ROOT = "/"
    internal const val PATH_PROMETHEUS = "/prometheus"
  }

  @GetMapping(PATH_ROOT)
  fun root(): ResponseEntity<String> {
    return ResponseEntity
      .status(HttpStatus.MOVED_PERMANENTLY)
      .header(HttpHeaders.LOCATION, PATH_PROMETHEUS)
      .build()
  }
}
