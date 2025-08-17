package click.dobel.tado.exporter.test

import click.dobel.tado.exporter.apiclient.TadoApiClient
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus

object ApiMockMappings {

  const val USER_FULL_NAME = "Test User"
  const val USER_EMAIL = "username@tado.test"
  const val HOME_ID = 123456

  fun apiPath(subPath: String) = TadoApiClient.BASE_URL + subPath

  fun successfulMeMapping() =
    WireMock.get(apiPath(TadoApiClient.ME_PATH))
      .willReturn(
        WireMock.aResponse()
          .applicationJson()
          .withStatus(HttpStatus.OK.value())
          .withBody(
            """
          {
              "name": "$USER_FULL_NAME",
              "email": "$USER_EMAIL",
              "username": "$USER_EMAIL",
              "id": "0123456789abcdef01234567",
              "homes": [
                  {
                      "id": $HOME_ID,
                      "name": "Home"
                  }
              ],
              "locale": "de",
              "mobileDevices": [
                  {
                      "name": "My Smartphone",
                      "id": 123456,
                      "settings": {
                          "geoTrackingEnabled": true,
                          "onDemandLogRetrievalEnabled": false,
                          "pushNotifications": {
                              "lowBatteryReminder": true,
                              "awayModeReminder": true,
                              "homeModeReminder": true,
                              "openWindowReminder": false,
                              "energySavingsReportReminder": false
                          }
                      },
                      "deviceMetadata": {
                          "platform": "Android",
                          "osVersion": "9",
                          "model": "Some Phone",
                          "locale": "de"
                      }
                  }
              ]
          }
        """.trimIndent()
          )
      )

  fun successfulHomesMapping() =
    WireMock.get(apiPath("${TadoApiClient.HOMES_PATH}/$HOME_ID"))
      .willReturn(
        WireMock.aResponse()
          .applicationJson()
          .withStatus(HttpStatus.OK.value())
          .withBody(
            """
          {
              "id": $HOME_ID,
              "name": "Home",
              "dateTimeZone": "Europe/Berlin",
              "dateCreated": "2019-01-01T12:00:00.000Z",
              "temperatureUnit": "CELSIUS",
              "partner": null,
              "simpleSmartScheduleEnabled": true,
              "awayRadiusInMeters": 400.00,
              "installationCompleted": true,
              "skills": [],
              "christmasModeEnabled": true,
              "showAutoAssistReminders": true,
              "contactDetails": {
                  "name": "$USER_FULL_NAME",
                  "email": "$USER_EMAIL",
                  "phone": "+49123123123123"
              },
              "address": {
                  "addressLine1": "Musterstr. 42",
                  "addressLine2": null,
                  "zipCode": "12345",
                  "city": "Musterstadt",
                  "state": null,
                  "country": "DEU"
              },
              "geolocation": {
                  "latitude": 50.986269,
                  "longitude": 6.890626
              },
              "consentGrantSkippable": true
          }
         """.trimIndent()
          )
      )
}
