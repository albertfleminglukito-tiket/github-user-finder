package com.fleming.githubuserfinder.ui.main.pagination

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.paging.PageKeyedDataSource
import com.fleming.githubuserfinder.Constants
import com.fleming.githubuserfinder.R
import com.fleming.githubuserfinder.base.scheduler.BaseSchedulerProvider
import com.fleming.githubuserfinder.domain.entity.User
import com.fleming.githubuserfinder.domain.usecase.GetUsersUseCase
import com.fleming.githubuserfinder.TestSchedulerProvider
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class UserDataSourceTest {

    @get:Rule
    var mInstantTaskExecutorRule = InstantTaskExecutorRule()

    private val mKeyword = "asd"
    private val mGetUsersUseCase: GetUsersUseCase = mock()
    private val mSchedulers: BaseSchedulerProvider = TestSchedulerProvider()

    private val mLoadingStateObserver: Observer<Boolean> = mock()
    private val mErrorStateObserver: Observer<Int> = mock()

    private lateinit var mDataSource: UserDataSource

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        mDataSource = spy(UserDataSource(mKeyword, mGetUsersUseCase, mSchedulers, mock()))

        mDataSource.showLoadingState.observeForever(mLoadingStateObserver)
        mDataSource.showErrorState.observeForever(mErrorStateObserver)
    }

    @Test
    fun `loadInitial - success`() {
        // given
        val responseList = listOf(User(), User())
        val callback: PageKeyedDataSource.LoadInitialCallback<Int, User> = mock()
        whenever(mGetUsersUseCase.execute(mKeyword, 1, Constants.ITEM_PER_PAGE)).thenReturn(Single.just(responseList))

        // when
        mDataSource.loadInitial(mock(), callback)

        // then
        argumentCaptor<Boolean> {
            then(mLoadingStateObserver).should(times(2)).onChanged(capture())
            Assert.assertTrue(firstValue)
            Assert.assertFalse(secondValue)
        }
        verify(callback).onResult(responseList, 0, responseList.size, 0, 2)
        then(mErrorStateObserver).shouldHaveZeroInteractions()
    }

    @Test
    fun `loadInitial - error`() {
        // given
        val responseList = listOf(User(), User())
        val callback: PageKeyedDataSource.LoadInitialCallback<Int, User> = mock()
        whenever(mGetUsersUseCase.execute(mKeyword, 1, Constants.ITEM_PER_PAGE)).thenReturn(Single.error(Throwable()))

        // when
        mDataSource.loadInitial(mock(), callback)

        // then
        argumentCaptor<Boolean> {
            then(mLoadingStateObserver).should(times(2)).onChanged(capture())
            Assert.assertTrue(firstValue)
            Assert.assertFalse(secondValue)
        }
        verify(callback, never()).onResult(responseList, 0, responseList.size, 0, 2)
        then(mErrorStateObserver).should().onChanged(R.string.message_get_user_error)
    }

    @Test
    fun `loadAfter - success`() {
        // given
        val page = 2
        val responseList = listOf(User(), User())
        val params = PageKeyedDataSource.LoadParams(1, Constants.ITEM_PER_PAGE)
        val callback: PageKeyedDataSource.LoadCallback<Int, User> = mock()
        whenever(mGetUsersUseCase.execute(mKeyword, page, Constants.ITEM_PER_PAGE)).thenReturn(Single.just(responseList))

        // when
        mDataSource.loadAfter(params, callback)

        // then
        argumentCaptor<Boolean> {
            then(mLoadingStateObserver).should(times(2)).onChanged(capture())
            Assert.assertTrue(firstValue)
            Assert.assertFalse(secondValue)
        }
        verify(callback).onResult(responseList, 2)
        then(mErrorStateObserver).shouldHaveZeroInteractions()
    }

    @Test
    fun `loadAfter - error`() {
        // given
        val page = 2
        val responseList = listOf(User(), User())
        val params = PageKeyedDataSource.LoadParams(1, Constants.ITEM_PER_PAGE)
        val callback: PageKeyedDataSource.LoadCallback<Int, User> = mock()
        whenever(mGetUsersUseCase.execute(mKeyword, page, Constants.ITEM_PER_PAGE)).thenReturn(Single.error(Throwable()))

        // when
        mDataSource.loadAfter(params, callback)

        // then
        argumentCaptor<Boolean> {
            then(mLoadingStateObserver).should(times(2)).onChanged(capture())
            Assert.assertTrue(firstValue)
            Assert.assertFalse(secondValue)
        }
        verify(callback, never()).onResult(responseList, 2)
        then(mErrorStateObserver).should().onChanged(R.string.message_get_user_error)
    }

    @Test
    fun `loadBefore - do nothing`() {
        // when
        mDataSource.loadBefore(mock(), mock())
        //then
        then(mLoadingStateObserver).shouldHaveZeroInteractions()
        then(mErrorStateObserver).shouldHaveZeroInteractions()
    }

    @Test
    fun `getErrorMessageResource - keyword empty`() {
        getErrorMessageResource(Throwable(GetUsersUseCase.ERROR_KEYWORD_EMPTY), R.string.message_get_user_keyword_empty_error)
    }

    @Test
    fun `getErrorMessageResource - result empty`() {
        getErrorMessageResource(Throwable(GetUsersUseCase.ERROR_RESULT_EMPTY), R.string.message_get_user_empty_result_error)
    }

    @Test
    fun `getErrorMessageResource - default`() {
        getErrorMessageResource(Throwable(), R.string.message_get_user_error)
    }

    private fun `getErrorMessageResource`(error: Throwable, expected: Int) {
        // when
        val result = mDataSource.getErrorMessageResource(error)
        // then
        Assert.assertEquals(expected, result)
    }

}
