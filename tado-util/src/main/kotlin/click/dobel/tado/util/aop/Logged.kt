package click.dobel.tado.util.aop

import java.lang.annotation.Inherited

@Target(
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER
)
@Inherited
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Logged(
  val message: String,
  val params: Array<String> = []
)
