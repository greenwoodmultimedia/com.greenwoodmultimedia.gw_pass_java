import classes.GwConsole
import classes.GwFile
import classes.GwSecurity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import entites.GwPasswordList
import entites.GwService
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64


fun main(args: Array<String>)
{
    //On affiche l'en-tête de l'application
    GwConsole.viderConsole()
    GwConsole.afficherEnTeteApplication();

    //Si aucun paramètre, on afficher un message d'erreur
    if(args.isEmpty())
    {
        GwConsole.ecrireConsole("Veuillez fournir des arguments ! Écrire '-aide-arguments' afin d'obtenir de l'aide !")
    }
    //Si on a un paramètre et que celui est l'aide
    else if(args.size == 1 && args[0] == "-aide-arguments")
    {
        GwConsole.afficherAideApplication()
    }
    else if(args.size == 2 && args[0] == "-fichier")
    {
        try
        {
            //Vérifier si le fichier existe selon le chemin fourni
            val gwFichier = GwFile(args[1]);
            val gwPasswordList:GwPasswordList;
            val gwSecurity:GwSecurity;

            //Si le fichier existe, on extrait les donnés
            if(gwFichier.obtenir_contenu().isNotEmpty())
            {
                //On va chercher la liste des mot de passe
                gwPasswordList = gwFichier.obtenir_objet(GwPasswordList::class.java) as GwPasswordList
                val sel:ByteArray = Base64.getDecoder().decode(gwPasswordList.sel)
                val vecteurInitialisation = Base64.getDecoder().decode(gwPasswordList.vecteur_initialisation)
                val secretKey = GwSecurity.generateSecretKey(gwPasswordList.mot_de_passe.toCharArray(), sel)
                gwSecurity = GwSecurity(secretKey, vecteurInitialisation)
                GwConsole.ecrireConsole("Le fichier json a été trouvé !")
            }
            else
            {
                //On va demander le courriel
                GwConsole.ecrireConsole("Le fichier que vous avez spécifié n'existe pas. Nous allons le créer...")
                val courriel = GwConsole.demanderQuestion("Veuillez entrer votre courriel")
                val motDePasse = GwConsole.demanderMotDePasse("Veuillez entrer votre mot de passe")
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
                val dateActuelle = formatter.format(LocalDateTime.now())
                val sel:ByteArray = GwSecurity.genererSel()
                val vecteurInitialisation = GwSecurity.genererVecteurInitialisation()
                val secretKey = GwSecurity.generateSecretKey(GwSecurity.sha256(motDePasse).toCharArray(), sel)
                gwSecurity = GwSecurity(secretKey, vecteurInitialisation)

                //On va créer l'objet qui sera contenu dans le fichier JSON
                gwPasswordList = GwPasswordList(
                    GwConsole.VERSION,
                    gwSecurity.encrypter(courriel)!!,
                    GwSecurity.sha256(motDePasse),
                    Base64.getEncoder().encodeToString(sel),
                    Base64.getEncoder().encodeToString(vecteurInitialisation),
                    gwSecurity.encrypter(dateActuelle.toString())!!,
                    gwSecurity.encrypter(dateActuelle.toString())!!,
                    mutableListOf()
                )

                //On écrit les données au fichier.
                gwFichier.sauvegarder_json(gwPasswordList)

                //On indique à l'utilisateur que tout c'est bien passé.
                GwConsole.ecrireConsole("Le fichier json a été créé !")
            }

            GwConsole.ecrireConsole("Veuillez vous authentifié !")

            //On va tenter la connexion
            val courriel = GwConsole.demanderQuestion("Veuillez entrer votre courriel")
            val motDePasse = GwConsole.demanderMotDePasse("Veuillez entrer votre mot de passe")

            //On va vérifier le mot de passe de l'utilisateur
            if(gwPasswordList.mot_de_passe != GwSecurity.sha256(motDePasse) || courriel != gwSecurity.decrypter(gwPasswordList.courriel))
            {
                GwConsole.ecrireConsole("Erreur d'authentification ! Le programme va se terminer !")
                return
            }

            //On va vider la console afin d'indiquer que la connexion s'est bien effectué
            GwConsole.ecrireConsole("Vous êtes bel et bien connecté !")
            val dateDerniereConnexion = gwSecurity.decrypter(gwPasswordList.derniere_date_acces)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
            val dateActuelle = formatter.format(LocalDateTime.now())
            GwConsole.ecrireConsole("Votre dernière connexion : $dateDerniereConnexion")
            gwPasswordList.derniere_date_acces = gwSecurity.encrypter(dateActuelle)!!
            gwFichier.sauvegarder_json(gwPasswordList)

            var estTermine = false

            while(!estTermine)
            {
                GwConsole.afficherPrefixe()
                val commande:String = readln()

                if(commande == "quitter" || commande == "q")
                {
                    GwConsole.ecrireConsole("Déconnexion !")
                    estTermine = true;
                    continue;
                }
                else if(commande == "vider" || commande == "v")
                {
                    GwConsole.viderConsole()
                    GwConsole.afficherEnTeteApplication()
                }
                else if(commande == "liste_service" || commande == "ls")
                {
                    if(gwPasswordList.liste_service.isEmpty())
                    {
                        GwConsole.ecrireConsole("Aucun service à afficher.")
                        continue;
                    }

                    GwConsole.ecrireConsole("--- DÉBUT DE LA LISTE DES SERVICES ---")
                    for(service in gwPasswordList.liste_service)
                    {
                        val nomService = gwSecurity.decrypter(service.nom)
                        GwConsole.ecrireConsole(nomService!!)
                    }
                    GwConsole.ecrireConsole("--- FIN DE LA LISTE DES SERVICES ---")
                }
                else if(commande == "voir_service" || commande == "vs")
                {
                    val nomService = GwConsole.demanderQuestion("Entrer le nom du service")

                    if(nomService.isEmpty())
                    {
                        GwConsole.ecrireConsole("Vous n'avez entré aucun nom de service.")
                        continue;
                    }

                    if(gwPasswordList.liste_service.isEmpty())
                    {
                        GwConsole.ecrireConsole("Aucun service n'existe dans le gestionnaire de clé.")
                        continue;
                    }

                    for(service in gwPasswordList.liste_service)
                    {
                        val nomServiceDecrypte = gwSecurity.decrypter(service.nom)
                        if(nomServiceDecrypte == nomService)
                        {
                            GwConsole.ecrireConsole("Nom du service: $nomService")
                            GwConsole.ecrireConsole("Identifiant: " + gwSecurity.decrypter(service.identifiant))
                            GwConsole.ecrireConsole("Mot de passe: " + gwSecurity.decrypter(service.mot_de_passe))
                        }
                    }
                }
                else if(commande == "ajouter_service" || commande == "as")
                {
                    val nomService = GwConsole.demanderQuestion("Veuillez entrer le nom du service")

                    if(nomService.isEmpty())
                    {
                        GwConsole.ecrireConsole("Vous n'avez pas entré de nom de service.")
                        continue;
                    }

                    val identifiant = GwConsole.demanderQuestion("Veuillez entrer l'identifant du service")

                    if(identifiant.isEmpty())
                    {
                        GwConsole.ecrireConsole("Vous n'avez pas entré d'identifiant pour le service.")
                        continue;
                    }

                    val motDePasseService = GwConsole.demanderMotDePasse("Veuillez entrer le mot de passe du service")

                    if(motDePasseService.isEmpty())
                    {
                        GwConsole.ecrireConsole("Vous n'avez pas entré de mot de passe pour le service.")
                        continue;
                    }

                    val service = GwService(gwSecurity.encrypter(nomService)!!, gwSecurity.encrypter(identifiant)!!, gwSecurity.encrypter(motDePasseService)!!)

                    val resultat = gwPasswordList.liste_service.add(service)

                    gwPasswordList.liste_service.sortWith(Comparator{ x, y -> gwSecurity.decrypter(x.nom)!!.compareTo(
                        gwSecurity.decrypter(y.nom)!!
                    ) })

                    gwFichier.sauvegarder_json(gwPasswordList)

                    if(resultat)
                    {
                        GwConsole.ecrireConsole("L'élément a bel et bien été ajouté !")
                    }
                }
                else if(commande == "supprimer_service" || commande == "ss")
                {
                    val nomService = GwConsole.demanderQuestion("Veuillez entrer le nom du service")

                    if(nomService.isEmpty())
                    {
                        GwConsole.ecrireConsole("Vous devez entrer le nom du service.")
                        continue;
                    }

                    val elementSupprime = gwPasswordList.liste_service.removeIf {
                        if(gwSecurity.decrypter(it.nom) == nomService)
                        {
                            return@removeIf true;
                        }

                        return@removeIf false;
                    }

                    gwPasswordList.liste_service.sortWith(Comparator{ x, y -> gwSecurity.decrypter(x.nom)!!.compareTo(
                        gwSecurity.decrypter(y.nom)!!
                    ) })

                    if(elementSupprime)
                    {
                        gwFichier.sauvegarder_json(gwPasswordList)
                        GwConsole.ecrireConsole("L'élément a été supprimé avec succès !")
                        continue;
                    }

                    GwConsole.ecrireConsole("L'élément n'a pas été supprimé avec succès.")
                }
                else if(commande == "aide" || commande == "a")
                {
                    GwConsole.ecrireArgumentConsole("'aide' ou 'a' Permet d'afficher l'aide.")
                    GwConsole.ecrireArgumentConsole("'liste_service' ou 'ls' Permet de la liste des services.")
                    GwConsole.ecrireArgumentConsole("'voir_service' ou 'vs' Permet de voir un des services.")
                    GwConsole.ecrireArgumentConsole("'ajouter_service' ou 'as' Permet d'ajouter un service.")
                    GwConsole.ecrireArgumentConsole("'supprimer_service' ou 'ss' Permet de supprimer un service.")
                    GwConsole.ecrireArgumentConsole("'quitter' ou 'q' Permet de quitter gw_pass.")
                    GwConsole.ecrireArgumentConsole("'vider' ou 'v' Permet de vider la console gw_pass.")
                }
                else if(commande.isEmpty())
                {
                    continue;
                }
                else
                {
                    GwConsole.ecrireConsole("Cette commande n'est pas reconnu. Utilisez la commande 'aide' pour obtenir les commandes.")
                }
            }

            GwConsole.ecrireConsole("Bonne journée !")
        }
        catch (e:IllegalArgumentException)
        {
            GwConsole.ecrireConsole("Le nom de fichier que vous avez fourni est vide !")
        }
        catch (e:NullPointerException)
        {
            println()
            GwConsole.ecrireConsole("Veuillez lancer le logiciel dans une vrai console !")
        }
        catch (e:java.lang.Exception) {
            println()
            GwConsole.ecrireConsole("Une erreur innattendue survenue !")
            GwConsole.ecrireConsole("RAISON: " + e.message)
            GwConsole.ecrireConsole("STACKTRACE: " + e.stackTraceToString())
        }
    }
    else
    {
        GwConsole.ecrireConsole("Veuillez fournir des arguments valides ! Écrire '-aide-arguments' afin d'obtenir de l'aide !")
    }
}