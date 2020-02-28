package sqlc.runtime

import java.lang.RuntimeException
import java.sql.Statement
import java.time.Duration

/**
 * Query is an abstract superclass for all query types.
 */
abstract class Query {
    @Volatile
    var timeout: Duration? = null

    private var _statement: Statement? = null
    protected var statement: Statement
        @Synchronized get() {
            return this._statement ?: throw RuntimeException("Cannot get Statement before Query is executed")
        }
        @Synchronized set(value) {
            val timeout = this.timeout
            if (timeout != null) {
                value.queryTimeout = timeout.seconds.toInt()
            }
            this._statement = value
        }

    /**
     * Cancels the statement.
     * @throws RuntimeException if called before the statement is executed.
     */
    fun cancel() {
        this.statement.cancel()
    }
}

/**
 * RowQuery implements sqlc :one commands.
 */
abstract class RowQuery<T> : Query() {
    abstract fun execute(): T
}

/**
 * ListQuery implements sqlc :many commands.
 */
abstract class ListQuery<T> : Query() {
    abstract fun execute(): List<T>
}

/**
 * ExecuteQuery implements sqlc :exec commands
 */
abstract class ExecuteQuery : Query() {
    abstract fun execute()
}

/**
 * ExecuteUpdateQuery implements sqlc :execUpdate commands
 */
abstract class ExecuteUpdateQuery : Query() {
    abstract fun execute(): Int
}