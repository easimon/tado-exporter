package click.dobel.tado.server

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import java.net.URI
import javax.inject.Singleton

@Controller
class BaseRedirect {

  @Get("/")
  fun root(): HttpResponse<String> {
    return HttpResponse.redirect(URI("/prometheus"))
  }
}
