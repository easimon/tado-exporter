package click.dobel.tado.server

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import java.net.URI

@Controller
class BaseRedirect {
  companion object {
    internal const val PATH_ROOT = "/"
    internal const val PATH_PROMETHEUS = "/prometheus"
  }

  @Get(PATH_ROOT)
  fun root(): HttpResponse<String> {
    return HttpResponse.temporaryRedirect(URI(PATH_PROMETHEUS))
  }
}
