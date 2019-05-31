package com.petrlr14.mvvm.database.viewmodels

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.petrlr14.mvvm.database.RoomDB
import com.petrlr14.mvvm.database.entities.GitHubRepo
import com.petrlr14.mvvm.database.repositories.GitHubRepoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GitHubRepoViewModel(private val app: Application) : AndroidViewModel(app) {

    private val repository: GitHubRepoRepository

    init {
        val repoDao=RoomDB.getInstance(app).repoDao()
        repository= GitHubRepoRepository(repoDao)
    }

    private suspend fun insert(repo:GitHubRepo)=repository.insert(repo)

    fun getAll():LiveData<List<GitHubRepo>>{
        return repository.getAll()
    }

    private suspend fun nuke()= repository.nuke()

    fun retrieveRepos(user: String) = viewModelScope.launch(){
        this@GitHubRepoViewModel.nuke()//TODO OBTENEMOS NUESTRA TABLA LIMPIA

        val response = repository.retrieveReposAsync(user).await()

        if (response.isSuccessful) with(response){
            //TODO WITH TRABAJA CON LA VARIABLE QUE LE MANDAS
            this.body()?.forEach{
                this@GitHubRepoViewModel.insert(it)
            }//TODO PUEDE QUE VENGA VACIO POR ESO EL NULO
        }
        else with(response){
            when(this.code()){
                404->{
                    Toast.makeText(app, "User not found", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}