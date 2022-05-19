package entites

data class GwPasswordList(
    val version:String,
    val courriel:String,
    val mot_de_passe:String,
    val sel:String,
    val vecteur_initialisation:String,
    val date_initialisation:String,
    var derniere_date_acces:String,
    val liste_service:MutableList<GwService>
)

data class GwService(
    val nom:String,
    val identifiant:String,
    val mot_de_passe:String
)
