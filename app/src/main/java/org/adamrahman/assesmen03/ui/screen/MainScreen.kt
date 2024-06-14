package org.adamrahman.assesmen03.ui.screen

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import org.adamrahman.assesmen03.BuildConfig
import org.adamrahman.assesmen03.R
import org.adamrahman.assesmen03.model.Bunga
import org.adamrahman.assesmen03.model.User
import org.adamrahman.assesmen03.navigation.Screen
import org.adamrahman.assesmen03.network.ApiStatus
import org.adamrahman.assesmen03.network.BungaApi
import org.adamrahman.assesmen03.network.UserDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.adamrahman.assesmen03.ui.theme.YourFlowerTheme

@Composable
fun ProfileImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String?,
    defaultImageRes: Int,
    imageSize: Dp = 48.dp
) {
    val painter = rememberImagePainter(
        data = imageUrl,
        builder = {
            crossfade(true)
            placeholder(defaultImageRes)
        }
    )

    Box(
        modifier = modifier
            .size(imageSize)
            .clip(shape = CircleShape)
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize()
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(User())

    val viewModel: MainViewModel = viewModel()
    val errorMessage by viewModel.errorMessage

    var showDialog by remember { mutableStateOf(false) }
    var showBungaDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var currentBungaId by remember { mutableStateOf("") }

    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    val launcher = rememberLauncherForActivityResult(CropImageContract()) {
        bitmap = getCroppedImage(context.contentResolver, it)
        if (bitmap != null) showBungaDialog = true
    }

    var showList by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = {
                        showList = !showList
                    }) {
                        Icon(
                            painter = painterResource(
                                if (showList) R.drawable.view_grid
                                else R.drawable.view_list
                            ),
                            contentDescription = stringResource(
                                if (showList) R.string.grid
                                else R.string.list
                            ),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = {
                        if (user.email.isEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch { signIn(context, dataStore) }
                        } else {
                            showDialog = true
                        }
                    }) {
                        if (!user.photoUrl.isNullOrEmpty()) {
                            ProfileImage(
                                imageUrl = user.photoUrl,
                                contentDescription = stringResource(R.string.profil),
                                defaultImageRes = R.drawable.account_circle,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.account_circle),
                                contentDescription = stringResource(R.string.profil),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { navController.navigate(Screen.About.route) }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = stringResource(R.string.about),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (user.email.isNotEmpty()) {
                    FloatingActionButton(onClick = {
                        val options = CropImageContractOptions(
                            null, CropImageOptions(
                                imageSourceIncludeGallery = false,
                                imageSourceIncludeCamera = true,
                                fixAspectRatio = true
                            )
                        )
                        launcher.launch(options)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(id = R.string.tambah_bunga)
                        )
                    }
                }
            }
        }
    ) { padding ->
        ScreenContent(
            showList = showList,
            viewModel = viewModel,
            userId = user.email,
            modifier = Modifier.padding(padding),
            onDeleteRequest = { id ->
                showDeleteDialog = true
                currentBungaId = id
                Log.d("MainScreen", "Current Bunga ID: $currentBungaId")
            },
            isUserLoggedIn = user.email.isNotEmpty()
        )

        if (showDialog) {
            ProfilDialog(
                user = user,
                onDismissRequest = { showDialog = false }) {
                CoroutineScope(Dispatchers.IO).launch { signOut(context, dataStore) }
                showDialog = false
            }
        }
        if (showBungaDialog) {
            BungaDialog(
                bitmap = bitmap,
                onDismissRequest = { showBungaDialog = false }) { nama, namalatin ->
                viewModel.saveData(user.email, nama, namalatin, bitmap!!)
                showBungaDialog = false
            }
        }

        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }

        if (showDeleteDialog) {
            DeleteConfirmationDialog(
                onDismissRequest = { showDeleteDialog = false },
                onConfirm = {
                    Log.d("MainScreen", "Deleting Bunga ID: $currentBungaId")
                    viewModel.deleteData(user.email, currentBungaId)
                    showDeleteDialog = false
                }
            )
        }
    }
}

@Composable
fun ScreenContent(
    showList: Boolean,
    viewModel: MainViewModel,
    userId: String,
    modifier: Modifier,
    onDeleteRequest: (String) -> Unit,
    isUserLoggedIn: Boolean
) {
    val context = LocalContext.current
    val data by viewModel.data
    val status by viewModel.status.collectAsState()

    LaunchedEffect(userId) {
        viewModel.retrieveData(userId)
    }

    when (status) {
        ApiStatus.LOADING -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        ApiStatus.SUCCESS -> {
            if (showList) {
                LazyVerticalGrid(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(data.filter { it.auth.isEmpty() || it.auth == userId }) { bunga ->
                        ListItem(
                            bunga = bunga,
                            onDeleteRequest = onDeleteRequest,
                            isUserLoggedIn = isUserLoggedIn,
                            currentUserId = userId,
                            context = context
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(data.filter { it.auth.isEmpty() || it.auth == userId }) { bunga ->
                        ListItem(
                            bunga = bunga,
                            onDeleteRequest = onDeleteRequest,
                            isUserLoggedIn = isUserLoggedIn,
                            currentUserId = userId,
                            context = context
                        )
                    }
                }
            }
        }

        ApiStatus.FAILED -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.error))
                Button(
                    onClick = { viewModel.retrieveData(userId) },
                    modifier = Modifier.padding(top = 16.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(text = stringResource(id = R.string.try_again))
                }
            }
        }
    }
}
@Composable
fun ListItem(
    bunga: Bunga,
    onDeleteRequest: (String) -> Unit,
    isUserLoggedIn: Boolean,
    currentUserId: String,
    context: Context
) {
    if (bunga.auth.isEmpty() || bunga.auth == currentUserId) {
        Box(
            modifier = Modifier
                .padding(4.dp)
                .border(1.dp, Color.Gray),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .border(1.dp, Color.Gray)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(BungaApi.getBungaUrl(bunga.image))
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.gambar, bunga.nama),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.loading_img),
                    error = painterResource(id = R.drawable.broken_img),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(Color(red = 0f, green = 0f, blue = 0f, alpha = 0.5f))
                    .padding(4.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bunga.nama,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row {
                        Text(
                            text = " ${bunga.namalatin}",
                            fontStyle = FontStyle.Italic,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    if (isUserLoggedIn && bunga.auth == currentUserId) {
                        IconButton(
                            onClick = {
                                if (bunga.id.isNotEmpty()) {
                                    onDeleteRequest(bunga.id)
                                } else {
                                    Log.d("ListItem", "Invalid bunga ID")
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.hapus),
                                tint = Color.White
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            shareBunga(context, bunga.nama, bunga.namalatin)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_share),
                            contentDescription = stringResource(R.string.bagikan),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun shareBunga(context: Context, nama: String, namalatin: String) {
    val shareText = context.getString(
        R.string.template_bagikan,
        nama,
        namalatin,
        context.getString(R.string.app_name)
    )

    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}

private suspend fun signIn(context: Context, dataStore: UserDataStore) {
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, dataStore)
    } catch (e: GetCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(result: GetCredentialResponse, dataStore: UserDataStore) {
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
            val nama = googleIdToken.displayName ?: ""
            val email = googleIdToken.id
            val photoUrl = googleIdToken.profilePictureUri.toString()
            dataStore.saveData(User(nama, email, photoUrl))
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    }
    else {
        Log.e("SIGN-IN", "Error: unrecognized custom credential type.")
    }
}

private suspend fun signOut(context: Context, dataStore: UserDataStore) {
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        dataStore.saveData(User())
    } catch (e: ClearCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private fun getCroppedImage(
    resolver: ContentResolver,
    result: CropImageView.CropResult
): Bitmap? {
    if (!result.isSuccessful) {
        Log.e("IMAGE", "Error: ${result.error}")
        return null
    }

    val uri = result.uriContent ?: return null

    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        MediaStore.Images.Media.getBitmap(resolver, uri)
    } else {
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ScreenPreview() {
    YourFlowerTheme {
        MainScreen(rememberNavController())
    }
}