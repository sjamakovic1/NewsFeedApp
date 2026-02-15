package etf.ri.rma.newsfeedapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import etf.ri.rma.newsfeedapp.data.network.AppRepository
import etf.ri.rma.newsfeedapp.navigation.Navigation
import etf.ri.rma.newsfeedapp.theme.NewsFeedAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppRepository.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            NewsFeedAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Navigation()
                }
            }
        }
    }
}