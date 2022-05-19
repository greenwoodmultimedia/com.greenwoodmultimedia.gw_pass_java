package classes

import com.google.gson.GsonBuilder
import java.io.File
import java.lang.reflect.Type

class GwFile(nomFichier:String)
{
    private var fichier: File? = null;

    init {
        if(nomFichier.isEmpty())
        {
            throw IllegalArgumentException("Le nom du fichier ne peut être vide !")
        }
        this.fichier = File(nomFichier)
    }

    fun sauvegarder(contenu:String): Boolean
    {
        if(this.fichier?.canWrite() == true)
        {
            this.fichier?.writeText(contenu, Charsets.UTF_8)
            return true;
        }
        return false;
    }

    fun sauvegarder_json(objet:Any): Boolean
    {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val contenu = gson.toJson(objet)
        return this.sauvegarder(contenu)
    }

    fun obtenir_contenu(): String
    {
        return this.fichier?.readText(Charsets.UTF_8).toString()
    }

    fun obtenir_objet(type:Type): Any
    {
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.fromJson(this.obtenir_contenu(), type)
    }
}