package com.aimarsg.serietracker.ui.pantallas

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aimarsg.serietracker.NotificationID
import com.aimarsg.serietracker.R
import com.aimarsg.serietracker.data.Idioma
import com.aimarsg.serietracker.ui.SeriesViewModel
import com.aimarsg.serietracker.ui.theme.SerieTrackerTheme
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


/*************************************************************************************************
 *
 * App's settings screens . Two different screens are defined here, with same functionalities
 * but different UI, as they have different layout. One is for portrait mode and the other is
 * for landscape mode
 *
 ************************************************************************************************/


/**
 * Setting screen for portrait mode
 * @param viewModel apps main viewmodel
 */
@SuppressLint("Range")
@Composable
fun Ajustes(
    modifier: Modifier = Modifier,
    viewModel: SeriesViewModel
){
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
            .padding(horizontal = 50.dp)

    ) {
        val contentResolver = LocalContext.current.contentResolver
        val filename = stringResource(R.string.nombreFicheroDefault)

        // Code to create a new file and export the users data //
        val saverLauncher = rememberLauncherForActivityResult(contract = CreateDocument("text/plain")) { uri ->
            try {

                // Obtener el nombre del archivo del URI
                val cursor = uri?.let { contentResolver.query(it, null, null, null, null) }
                cursor?.use {
                    if (it.moveToFirst()) {
                        val displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))

                        // Escribir en el archivo
                        contentResolver.openFileDescriptor(uri, "w")?.use { descriptor ->
                            FileOutputStream(descriptor.fileDescriptor).use { fileOutputStream ->
                                fileOutputStream.write(
                                    (viewModel.seriesSiguiendoToJson()).toByteArray()
                                )

                                // Mostrar notificacion
                                fileSaved(displayName, context)
                            }
                        }
                    }
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        Row()
        // LANGUAGE SELECTOR
        {
            Column {
                Icon(painter = painterResource(R.drawable.baseline_translate_24), contentDescription = stringResource(R.string.SeleccionarIdioma))
            }

            Column (
            ){
                var expanded by remember { mutableStateOf(false) }
                val idiomaSeleccionado by viewModel.idioma.collectAsState(initial = Idioma.Castellano)

                Text(text = stringResource(R.string.SeleccionarIdioma), modifier = Modifier.padding(start = 10.dp))
                TextButton(onClick = { expanded = true }) {
                    Text(text = idiomaSeleccionado.name)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    Idioma.entries.forEach { idioma ->
                        DropdownMenuItem(
                            text = { Text(idioma.name) },
                            onClick = {
                                expanded = false
                                viewModel.updateIdioma(idioma, context)
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.padding(20.dp))
        Divider(
            modifier = Modifier,
            thickness = 0.75.dp,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.padding(20.dp))

        // THEME SELECTOR
        Row {
            Column {
                Icon(
                    painter = painterResource(R.drawable.baseline_color_lens_24),
                    contentDescription = stringResource(R.string.SeleccionaModo)
                )
            }

            Column(
            ) {
                 val booleanState by viewModel.tema.collectAsState(initial = true)

                Text(
                    text = stringResource(R.string.SeleccionaModo),
                    modifier = Modifier.padding(start = 10.dp)
                )
                TextButton(
                    onClick = {
                        //expanded = true
                        if (booleanState){
                            viewModel.updateTheme(false)
                        }else{
                            viewModel.updateTheme(true)
                        }
                    }
                ) {
                    if (!booleanState){
                        Text(text = stringResource(R.string.ModoClaro))
                    }
                    else{
                        Text(text = stringResource(R.string.ModoOscuro))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.padding(20.dp))
        Divider(
            modifier = Modifier,
            thickness = 0.75.dp,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.padding(20.dp))

        // DOWNLOAD FILE
        Row {
            Icon(painter = painterResource(R.drawable.baseline_download_24), contentDescription = "")

            Text(text = stringResource(R.string.exportardatos),
                modifier = Modifier
                    .padding(start = 10.dp)
                    .clickable(onClick = {
                        saverLauncher.launch(filename)

                    })
            )
        }
    }
}

/**
 * Setting screen for landscape mode
 * @param viewModel apps main viewmodel
 */
@SuppressLint("Range")
@Composable
fun AjustesLanscape(
    modifier: Modifier = Modifier,
    viewModel: SeriesViewModel
){
    val context = LocalContext.current
    val contentResolver = LocalContext.current.contentResolver
    val filename = stringResource(R.string.nombreFicheroDefault)

    // Code to create a new file and export the users data //
    val saverLauncher = rememberLauncherForActivityResult(contract = CreateDocument("application/json")) { uri ->
        try {

            // Obtener el nombre del archivo del URI
            val cursor = uri?.let { contentResolver.query(it, null, null, null, null) }
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))

                    // Escribir en el archivo
                    contentResolver.openFileDescriptor(uri, "w")?.use { descriptor ->
                        FileOutputStream(descriptor.fileDescriptor).use { fileOutputStream ->
                            fileOutputStream.write(
                                (viewModel.seriesSiguiendoToJson()).toByteArray()
                            )
                            // Mostrar notificacion
                            fileSaved(displayName, context)
                        }
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    Row(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
            .padding(start = 10.dp, end = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(30.dp)

    ) {
        Column {

            // LANGUAGE SELECTOR
            Row{
                Column {
                    Icon(painter = painterResource(R.drawable.baseline_translate_24), contentDescription = stringResource(R.string.SeleccionarIdioma))
                }

                Column (
                ){
                    var expanded by remember { mutableStateOf(false) }
                    val idiomaSeleccionado by viewModel.idioma.collectAsState(initial = Idioma.Castellano)
                    Text(text = stringResource(R.string.SeleccionarIdioma), modifier = Modifier.padding(start = 10.dp))
                    TextButton(onClick = { expanded = true }) {
                        Text(text = idiomaSeleccionado.name)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        Idioma.entries.forEach { idioma ->
                            DropdownMenuItem(
                                text = { Text(idioma.name) },
                                onClick = {
                                    expanded = false
                                    viewModel.updateIdioma(idioma, context)
                                }
                            )
                        }
                    }
                }
            }
        }

        // THEME SELECTOR
        Column {
            Row {
                Column {
                    Icon(
                        painter = painterResource(R.drawable.baseline_color_lens_24),
                        contentDescription = stringResource(R.string.SeleccionaModo)
                    )
                }

                Column(
                ) {
                    val booleanState by viewModel.tema.collectAsState(initial = true)
                    Text(
                        text = stringResource(R.string.SeleccionaModo),
                        modifier = Modifier.padding(start = 10.dp)
                    )
                    TextButton(
                        onClick = {
                            //expanded = true
                            if (booleanState){
                                viewModel.updateTheme(false)
                            }else{
                                viewModel.updateTheme(true)
                            }
                        }
                    ) {
                        if (!booleanState){
                            Text(text = stringResource(R.string.ModoClaro))
                        }
                        else{
                            Text(text = stringResource(R.string.ModoOscuro))
                        }
                    }
                }
            }
        }

        // DOWNLOAD FILE
        Column {
            Row {
                Icon(painter = painterResource(R.drawable.baseline_download_24), contentDescription = "" )
                Text(text = stringResource(R.string.exportardatos), modifier = Modifier
                    .padding(start = 10.dp)
                    .clickable(onClick = {
                        saverLauncher.launch(filename)
                    }))
            }
        }
    }
}

/**
 * Function to generate a notification to inform the user that the file has been saved successfully
 * Displays a notification with the generated filename
 * @param fileName the name of the saved file
 * @param context apps context
 */
fun fileSaved(fileName: String, context: Context) {

    // Show user created notification
    val builder = NotificationCompat.Builder(context, "0")
        .setSmallIcon(R.drawable.baseline_download_24)
        .setContentTitle(context.getString(R.string.archivoDescargadoNot))
        .setContentText(context.getString(R.string.descargadoContent) + fileName)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notify(NotificationID.USER_CREATED.id, builder.build())
    }
}

@Preview(widthDp = 640, heightDp = 360, showBackground = true)
@Composable
fun PreviewAjustes (){
    SerieTrackerTheme(content = {
        //AjustesLanscape(modifier = Modifier)
    })
}