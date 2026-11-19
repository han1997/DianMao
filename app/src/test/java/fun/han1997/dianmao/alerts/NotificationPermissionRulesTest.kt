package `fun`.han1997.dianmao.alerts

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationPermissionRulesTest {
    @Test
    fun allowsTestNotificationOnlyWhenEveryDeliveryGateIsOpen() {
        assertTrue(canPostTestNotification(true, true, true))
        assertFalse(canPostTestNotification(false, true, true))
        assertFalse(canPostTestNotification(true, false, true))
        assertFalse(canPostTestNotification(true, true, false))
    }
}
