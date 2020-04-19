package ru.skillbranch.skillarticles.viewmodels.base

import androidx.lifecycle.SavedStateHandle

interface IViewModelState {
    /**
     * override this if need save state in bundle
     */
    fun save (outState: SavedStateHandle){
        //default empty implementation
    }
    /**
     * override this if need save restore in bundle
     */
        fun restore(savedState: SavedStateHandle) : IViewModelState{
        //default empty implementation
        return this
    }
}