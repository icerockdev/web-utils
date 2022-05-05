import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.icerockdev.sample.CustomResponse
import com.icerockdev.sample.QueryValues
import com.icerockdev.sample.TestResponse
import com.icerockdev.sample.main
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
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
        withTestApplication(moduleFunction = { main() }) {
            handleRequest(HttpMethod.Get, "/get?age=5&testValue=1&email=test@test.e&test=string&tmp=1&tmp=2&tmp=3") {
                addHeader(HttpHeaders.Accept, "application/json")
                addHeader(HttpHeaders.ContentType, "application/json")
            }.apply {
                Assert.assertEquals(HttpStatusCode.OK.value, response.status()?.value)
                val result = mapper.readValue(response.content, jacksonTypeRef<TestResponse>())
                Assert.assertNotNull(result)
                Assert.assertEquals(query.toString(), result?.message)
            }
        }
    }

    @Test
    fun `test custom object`() {
        val customResponse = CustomResponse(200, "Custom message", listOf(1, 2, 3))
        withTestApplication(moduleFunction = { main() }) {
            handleRequest(HttpMethod.Get, "/custom-object") {
                addHeader(HttpHeaders.Accept, "application/json")
                addHeader(HttpHeaders.ContentType, "application/json")
            }.apply {
                Assert.assertEquals(HttpStatusCode.OK.value, response.status()?.value)
                val result = mapper.readValue(response.content, jacksonTypeRef<CustomResponse>())
                Assert.assertNotNull(result)
                Assert.assertEquals(mapper.writeValueAsString(customResponse), mapper.writeValueAsString(result))
            }
        }
    }
}
