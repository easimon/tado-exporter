package click.dobel.tado.util.aop

import io.micronaut.aop.Around
import io.micronaut.context.annotation.Type
import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@Around
@Type(CallLoggingInterceptor::class)
annotation class Logged(
  val message: String,
  val params: Array<String> = []
)
