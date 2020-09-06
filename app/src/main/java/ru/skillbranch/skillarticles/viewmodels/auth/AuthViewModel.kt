package ru.skillbranch.skillarticles.viewmodels.auth

import androidx.lifecycle.SavedStateHandle
import ru.skillbranch.skillarticles.data.repositories.RootRepository
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand

class AuthViewModel(handle: SavedStateHandle) : BaseViewModel<AuthState>(handle, AuthState()) {
    private val repository = RootRepository
    init {
        subscribeOnDataSource(repository.isAuth()){isAuth, state ->
            state.copy(isAuth = isAuth)
        }
    }

    fun handleLogin(login:String, pass:String, dest:Int?){
        launchSafety {
            repository.login(login, pass)
            navigate(NavigationCommand.FinishLogin(dest))
        }
    }
}

data class  AuthState(val isAuth: Boolean = false): IViewModelState
