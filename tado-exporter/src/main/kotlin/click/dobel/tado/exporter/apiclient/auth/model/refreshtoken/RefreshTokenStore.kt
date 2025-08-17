package click.dobel.tado.exporter.apiclient.auth.model.refreshtoken

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

interface RefreshTokenStore {
  var token: RefreshToken?

  companion object {
    fun create(refreshTokenPath: String): RefreshTokenStore {
      return PersistentTokenStore(refreshTokenPath)
    }
  }
}

private class PersistentTokenStore(private val refreshTokenFile: File) : RefreshTokenStore {
  constructor(refreshTokenPath: String) : this(File(refreshTokenPath))

  companion object {
    private val objectMapper = ObjectMapper().findAndRegisterModules()
  }

  override var token: RefreshToken?
    get() = refreshTokenFile.readToken()?.takeIf { !it.isExpired }
    set(value) = refreshTokenFile.writeToken(value)

  init {
    runCatching {
      if (!refreshTokenFile.exists()) {
        if (refreshTokenFile.parentFile.mkdirs())
          refreshTokenFile.createNewFile()
        else logger.warn {
          "Directory ${refreshTokenFile.parentFile.absolutePath} does not exist, and is also not creatable."
        }
      }

      // ensure accessibility by reading and writing back
      if (!refreshTokenFile.canRead()) refreshTokenFile.setReadable(true)
      if (!refreshTokenFile.canWrite()) refreshTokenFile.setWritable(true)
      val t = token
      token = t
    }.onFailure { ex ->
      logger.warn {
        refreshTokenAccessError("access", ex)
      }
    }
  }

  private fun File.readToken(): RefreshToken? = runCatching {
    objectMapper.readValue(this, RefreshToken::class.java)
  }.onFailure { ex ->
    logger.trace {
      refreshTokenAccessError("read", ex)
    }
  }.getOrNull()

  private fun File.writeToken(value: RefreshToken?) = runCatching {
    if (value == null) writeText("")
    else objectMapper.writeValue(this, value)
  }.onFailure { ex ->
    logger.trace {
      refreshTokenAccessError("write", ex)
    }
  }.getOrDefault(Unit)

  private fun refreshTokenAccessError(type: String, ex: Throwable): String =
    "Could not $type refresh token file '${refreshTokenFile.absolutePath}': (${ex::class.simpleName}) ${ex.message}"
}
