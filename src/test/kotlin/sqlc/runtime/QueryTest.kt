package sqlc.runtime

import com.example.authors.QueriesImpl
import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.Exception
import java.sql.Connection
import java.sql.PreparedStatement
import java.time.Duration

internal class QueryTest {
    @Test
    fun testSetTimeout() {
        val testTimeout = 3
        val stmtMock = mock<PreparedStatement>()
        val connMock = mock<Connection>() {
            on {
                prepareStatement(any())
            } doReturn stmtMock
        }
        val queries = QueriesImpl(connMock)
        queries.deleteAuthor(1).apply {
            timeout = Duration.ofSeconds(testTimeout.toLong())
        }.execute()

        // Verify that the timeout is applied, before executing the statement
        inOrder(stmtMock) {
            verify(stmtMock).queryTimeout = testTimeout
            verify(stmtMock).execute()
        }
    }

    @Test
    fun testCancel() {
        val stmtMock = mock<PreparedStatement>()
        val connMock = mock<Connection>() {
            on {
                prepareStatement(any())
            } doReturn stmtMock
        }
        val queries = QueriesImpl(connMock)
        val query = queries.deleteAuthor(1)
        query.execute()
        query.cancel()

        inOrder(stmtMock) {
            verify(stmtMock).execute()
            verify(stmtMock).cancel()
        }
    }

    @Test
    fun testCancelBeforeExecute() {
        val stmtMock = mock<PreparedStatement>()
        val connMock = mock<Connection>() {
            on {
                prepareStatement(any())
            } doReturn stmtMock
        }
        val queries = QueriesImpl(connMock)
        val query = queries.deleteAuthor(1)
        assertThrows<Exception> {
            query.cancel()
        }
    }
}