package ads.pdm.intents

import ads.pdm.intents.databinding.ActivityMainBinding
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ads.pdm.intents.Constant.URL
import android.Manifest.permission.CALL_PHONE
import android.content.Intent.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {

    private val amb : ActivityMainBinding by lazy{
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var urlArl: ActivityResultLauncher<Intent>
    private lateinit var permissaoChamadaArl: ActivityResultLauncher<String>
    private lateinit var pegarImagemArl: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(amb.root)

        supportActionBar?.subtitle = "MainActivity"

        urlArl = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
        ) { resultado: ActivityResult ->
            if(resultado.resultCode == RESULT_OK){
                val urlRetornada = resultado.data?.getStringExtra(URL) ?: ""
                amb.urlTv.text = urlRetornada
            }
        }

        permissaoChamadaArl = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { concedida ->
            if (concedida!!) {
                chamarNumero(chamar = true)
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Permissão necessária para execução",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }

        pegarImagemArl = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { resultado: ActivityResult ->
            if(resultado.resultCode == RESULT_OK){
                //Recebendo path da Imagem
                val imagemUri = resultado.data?.data
                imagemUri?.let {
                    amb.urlTv.text = it.toString()
                }

                //Abrindo Visualizador
                val visualizarImagemIntent = Intent(ACTION_VIEW, imagemUri)
                startActivity(visualizarImagemIntent)
            }
        }

        amb.entrarUrlBt.setOnClickListener {
            val urlActivityIntent = Intent(this, UrlActivity::class.java)
            // val urlActivityIntent = Intent("SEGUNDA_TELA_DO_PROJETO_INTENTS")
            urlActivityIntent.putExtra(URL, amb.urlTv.text)
            urlArl.launch(urlActivityIntent)
        }

    }

    //Coloca o menu na ActionBar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    //trata das escolhas das opções de menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.viewMi -> {
                // Abrir o navegador na URL digitada pelo usuário
                val url = Uri.parse(amb.urlTv.text.toString())
                val navegadorIntent = Intent(ACTION_VIEW, url)
                startActivity(navegadorIntent)
                true
            }
            R.id.dialMi ->{
                chamarNumero(chamar = false)
                true
            }
            R.id.callMi -> {
                //Verificar a versão do Android
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    //Se superior ou igual a M
                    //Verificar se tem permissão e solicitar se necessário
                    if(checkSelfPermission(CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
                        //Fazer a Chamada
                        chamarNumero(chamar = true)
                    }
                    else{
                        //Solicitar Permissão
                        permissaoChamadaArl.launch(CALL_PHONE)
                    }
                }
                //Caso Contrário
                //Fazer a chamada
                else{
                    chamarNumero(chamar = true)
                }
                true
            }
            R.id.pickMi -> {
                val pegarImagemIntent = Intent(ACTION_PICK)
                val diretorioImagens = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path
                pegarImagemIntent.setDataAndType(Uri.parse(diretorioImagens), "image/*")
                pegarImagemArl.launch(pegarImagemIntent)
                true
            }
            R.id.chooserMi -> {
                val escolherAppIntent = Intent(ACTION_CHOOSER)
                val informacoesIntent = Intent(ACTION_VIEW, Uri.parse(amb.urlTv.text.toString()))
                escolherAppIntent.putExtra(EXTRA_TITLE, "Escolha Seu Navegador")
                escolherAppIntent.putExtra(EXTRA_INTENT, informacoesIntent)
                startActivity(escolherAppIntent)
                true
            }
            else -> { false }
        }
    }

    private fun chamarNumero(chamar: Boolean){
        val uri = Uri.parse("tel: ${amb.urlTv.text}")
        val intent = Intent(if (chamar) ACTION_CALL else ACTION_DIAL)
        intent.data = uri
        startActivity(intent)
    }

}