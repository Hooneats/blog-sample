package com.batch.task

import com.batch.payment.domain.payment.Payment
import com.batch.task.support.logger
import java.math.BigDecimal
import javax.sql.DataSource
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.stereotype.Component

@Component
class JobDataSetUpListener(
    private val dataSource: DataSource,
) : JobExecutionListener {
    val log by logger()

    override fun beforeJob(jobExecution: JobExecution) {
        val payments = (1..DATA_SET_UP_SIZE)
            .map { Payment(it.toBigDecimal(), it.toLong()) }

//        val sql = "insert into payment (amount, order_id, created_at, updated_at) values (?, ?, ?, ?)"
        val sql = "insert into payment (amount, order_id, created_at, updated_at) values (?, ?, now(), now())"
        val connection = dataSource.connection
        val statement = connection.prepareStatement(sql)!!

        try {
            for (payment in payments) {
                statement.apply {
                    setBigDecimal(1, BigDecimal.ZERO)
                    setLong(2, payment.orderId)
//                    setObject(3, LocalDateTime.now())
//                    setObject(4, LocalDateTime.now())
                    addBatch()
                }
            }
            statement.executeBatch()
        } catch (e: Exception) {
            throw e
        } finally {
            if (statement.isClosed.not()) {
                statement.close()
            }
            if (connection.isClosed.not()) {
                connection.close()
            }
        }
        log.info("data set up done")
    }

    override fun afterJob(jobExecution: JobExecution): Unit = Unit
}