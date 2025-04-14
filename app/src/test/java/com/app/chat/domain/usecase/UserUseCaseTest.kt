import com.app.chat.domain.model.User
import com.app.chat.domain.repository.UserRepository
import com.app.chat.domain.usecase.UserUseCase
import com.app.chat.domain.util.TypingObserver
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserUseCaseTest {

    private lateinit var repository: UserRepository
    private lateinit var userUseCase: UserUseCase

    private val dummyUserList = listOf(
        User(uid = "u1", name = "Anil"),
        User(uid = "u2", name = "Kumar")
    )

    @Before
    fun setup() {
        repository = mockk()
        userUseCase = UserUseCase(repository)
    }

    @Test
    fun `getUsersYouChattedWithWithTyping should emit enriched users with typing`() = runTest {
        every { repository.getUsersYouChattedWith("u123") } returns flowOf(dummyUserList)

        mockkObject(TypingObserver) // ✅

        dummyUserList.forEach {
            every { TypingObserver.observeUserWithTyping(it, "u123") } returns flowOf(it.copy(isTyping = true))
        }

        val result = userUseCase.getUsersYouChattedWithWithTyping("u123").first()
        assertEquals(true, result.all { it.isTyping })

        unmockkObject(TypingObserver)
    }

    @Test
    fun `getAllUsersExceptCurrent should emit user list`() = runTest {
        every { repository.getAllUsersExceptCurrent("u123") } returns flowOf(dummyUserList)

        val result = userUseCase.getAllUsersExceptCurrent("u123").first()
        assertEquals(dummyUserList, result)
    }
}
