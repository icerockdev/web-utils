import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.icerockdev.sample.CustomResponse
import com.icerockdev.sample.QueryValues
import com.icerockdev.sample.TestResponse
import com.icerockdev.webserver.applyDefaultConfiguration
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.junit.Assert
import org.junit.Test

class SampleTest {
    private val mapper = jacksonObjectMapper()

    @Test
    fun `test correct parse query`() {
        val query = QueryValues(
            email = "test@test.e",
            age = 5,
            testValue = 1,
            test = listOf("string"),
            tmp = listOf(1, 2, 3)
        )
        testApplication {
            val response = getClient().get("/get?age=5&testValue=1&email=test@test.e&test=string&tmp=1&tmp=2&tmp=3") {
                header(HttpHeaders.Accept, "application/json")
                header(HttpHeaders.ContentType, "application/json")
            }
            Assert.assertEquals(HttpStatusCode.OK.value, response.status.value)
            val result: TestResponse? = response.body()
            Assert.assertNotNull(result)
            Assert.assertEquals(query.toString(), result?.message)

        }
    }

    @Test
    fun `test custom object`() {
        val customResponse = CustomResponse(200, "Custom message", listOf(1, 2, 3))
        testApplication {
            val response = getClient().get("/custom-object") {
                header(HttpHeaders.Accept, "application/json")
                header(HttpHeaders.ContentType, "application/json")
            }

            Assert.assertEquals(HttpStatusCode.OK.value, response.status.value)
            val result: CustomResponse? = response.body()
            Assert.assertNotNull(result)
            Assert.assertEquals(mapper.writeValueAsString(customResponse), mapper.writeValueAsString(result))

        }
    }

    private fun ApplicationTestBuilder.getClient(): HttpClient = createClient {
        install(ContentNegotiation) {
            jackson {
                applyDefaultConfiguration()
                configure(SerializationFeature.INDENT_OUTPUT, false)
            }
        }
    }
}
