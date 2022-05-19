package classes

class GwConsole
{
    companion object {

        private const val PREFIXE = "gw_pass"
        const val VERSION = "1.0.0"

        fun afficherEnTeteApplication() {
            ecrireConsole("-----------------------------------------------");
            ecrireConsole("-                                             -");
            ecrireConsole("-               GW PASS JAVA                  -");
            ecrireConsole("-                  $VERSION                      -");
            ecrireConsole("-            GreenWood Multimedia             -");
            ecrireConsole("-                                             -");
            ecrireConsole("-----------------------------------------------");
        }

        fun afficherAideApplication() {
            ecrireArgument("'-aide-arguments' Permet d'afficher l'aide qui est actuellement afficher.", false)
            ecrireArgument(
                "'-fichier chemin_vers_le_fichier' Permet d'indiquer le chemin absolu ou relatif vers le fichier contenant les mots de passe.",
                true
            )
        }

        fun ecrireConsole(message: String) {
            println("$PREFIXE> $message")
        }

        fun demanderQuestion(question: String): String {
            print("$PREFIXE> $question : ")
            return readln()
        }

        fun demanderMotDePasse(question: String): String {
            print("$PREFIXE> $question : ")
            return String(System.console().readPassword()!!)
        }

        fun afficherPrefixe() {
            print("$PREFIXE> ")
        }

        fun ecrireArgument(message: String, est_requis: Boolean) {
            val texteOtionArgument = if (est_requis) "REQUIS   " else "OPTIONNEL"
            ecrireConsole("Argument | $texteOtionArgument | $message")
        }

        fun ecrireArgumentConsole(message: String) {
            ecrireConsole("Argument | $message")
        }

        fun viderConsole()
        {
            print("\u001b[H\u001b[2J")
        }
    }
}