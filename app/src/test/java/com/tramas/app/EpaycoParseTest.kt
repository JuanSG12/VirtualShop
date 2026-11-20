package com.tramas.app

import com.tramas.app.data.payment.EpaycoApi
import com.tramas.app.data.payment.PaymentState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

/** El pago solo se aprueba con una confirmación explícita de ePayco; una respuesta rara nunca cuenta como aprobada. */
@RunWith(AndroidJUnit4::class)
class EpaycoParseTest {
    @Test fun aprobadaPorCodigoEnData() {
        val r = EpaycoApi.parseValidation("""{"success":true,"data":{"x_cod_response":1,"x_response":"Aceptada","x_ref_payco":"123"}}""", "9")
        assertEquals(PaymentState.APPROVED, r.state); assertEquals("123", r.ref)
    }
    @Test fun aprobadaPorTextoEnRaiz() {
        assertEquals(PaymentState.APPROVED, EpaycoApi.parseValidation("""{"x_response":"Aceptada"}""", "9").state)
    }
    @Test fun aprobadaConDataAnidado() {
        assertEquals(PaymentState.APPROVED, EpaycoApi.parseValidation("""{"data":{"data":{"x_cod_response":"1"}}}""", "9").state)
    }
    @Test fun pendiente() {
        assertEquals(PaymentState.PENDING, EpaycoApi.parseValidation("""{"data":{"x_cod_response":3}}""", "9").state)
    }
    @Test fun rechazadaOFallida() {
        assertEquals(PaymentState.REJECTED, EpaycoApi.parseValidation("""{"data":{"x_cod_response":2,"x_response":"Rechazada"}}""", "9").state)
        assertEquals(PaymentState.REJECTED, EpaycoApi.parseValidation("""{"data":{"x_response":"Fallida"}}""", "9").state)
    }
    @Test fun respuestaDeErrorNuncaAprueba() {
        assertThrows(IllegalStateException::class.java) {
            EpaycoApi.parseValidation("""{"status":false,"message":"Error de datos o conexión.","data":{"status":"error"}}""", "9")
        }
    }
}
