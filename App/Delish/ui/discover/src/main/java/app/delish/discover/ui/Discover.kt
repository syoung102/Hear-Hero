package app.delish.discover.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import app.delish.compose.ui.AsyncImage
import app.delish.compose.ui.ScrollableBackground
import app.delish.compose.ui.verticalGradient
import app.delish.compose.view.CircularLoading
import app.delish.compose.view.EmptyView
import app.delish.discover.R
import app.delish.discover.vm.DiscoverViewModel
import app.delish.discover.vm.ViewEffect.OpenIngredientsSheet
import app.delish.discover.vm.ViewEvent
import com.elbehiry.model.RecipesItem
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun Discover(
    onDetails: (Int) -> Unit,
    onIngredients: () -> Unit,
    bottomBarPadding: PaddingValues,
    onIngredientSearch: (String) -> Unit
) {
    Discover(
        hiltViewModel(),
        bottomBarPadding = bottomBarPadding,
        onCuisineSearch = onIngredientSearch,
        onDetails = onDetails,
        onIngredients = onIngredients,
        onIngredientSearch = onIngredientSearch
    )
}

@Composable
internal fun Discover(
    viewModel: DiscoverViewModel,
    bottomBarPadding: PaddingValues,
    onCuisineSearch: (String) -> Unit,
    onDetails: (Int) -> Unit,
    onIngredients: () -> Unit,
    onIngredientSearch: (String) -> Unit
) {
    val viewState by viewModel.states.collectAsStateWithLifecycle()

    CircularLoading(
        isLoading = viewState.isLoading
    ) {
        Surface(modifier = Modifier
            .fillMaxSize()
            .padding(bottomBarPadding)) {

            LaunchedEffect(key1 = true) {
                viewModel.effects.onEach { effect ->
                    when (effect) {
                        is OpenIngredientsSheet -> {
                            onIngredients()
                        }
                    }
                }.launchIn(this)
            }
            AnimatedVisibility(visible = !viewState.hasError) {
                LazyColumn(
                    modifier = Modifier
                        .statusBarsPadding()
                ) {
                    item { MainView(
                        titleText = stringResource(id = R.string.welcome_to),
                        descText = stringResource(id = R.string.hear_hero),
                        imageResourceId = R.drawable.recipe_empty
                    ) }
                    item { LearnMoreButton() }
                    }

//                    item { Spacer(modifier = Modifier.padding(16.dp)) }
//                    item { HomeCuisines(viewState.cuisinesList, onCuisineSearch) }
//                    item { Spacer(modifier = Modifier.padding(50.dp)) }
                }
            }
            AnimatedVisibility(visible = viewState.hasError) {
                EmptyView(
                    titleText = stringResource(id = R.string.ops),
                    descText = stringResource(id = R.string.something_went_wrong),
                    imageResourceId = R.drawable.recipe_empty
                )
            }
        }
    }


@Composable
fun HeaderTitle() {
    Text(
        text = stringResource(id = R.string.welcome_to),
        style = MaterialTheme.typography.h6,
        fontSize = 34.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 1.dp
            )
    )
}

@Composable
fun SubTitle() {
    Text(
        text = stringResource(id = R.string.hear_hero),
        style = MaterialTheme.typography.h6,
        fontSize = 34.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                top = 2.dp,
                end = 50.dp,
                bottom = 25.dp
            )
    )
}

@Composable
fun MainView(
    modifier: Modifier = Modifier,
    imageResourceId : Int,
    titleText: String? = "",
    descText: String? = ""
) {
    ConstraintLayout(modifier = modifier.fillMaxSize()) {
        val (image, title, desc) = createRefs()
        Image(
            painter = painterResource(id = R.drawable.recipe_empty),
            contentDescription = "empty",
            modifier = Modifier
                .constrainAs(image) {
                linkTo(
                    start = parent.start,
                    end = parent.end,
                )
                    width = Dimension.ratio("1:2")
                    height = Dimension.ratio("1:1")

                }
        )
        Text(
            text = titleText ?: stringResource(id = R.string.welcome_to),
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center,
            color = Color.White,
            fontSize = 34.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp)
                .constrainAs(title) {
                    linkTo(
                        start = parent.start,
                        end = parent.end,
                    )
                    top.linkTo(image.bottom)
                }
        )
        Text(
            text = descText ?: stringResource(id = R.string.hear_hero),
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center,
            color = Color(15, 82, 186),
            fontSize = 34.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 20.dp)
                .constrainAs(desc) {
                    linkTo(
                        start = parent.start,
                        end = parent.end,
                    )
                    top.linkTo(title.bottom)
                }
        )
    }
}

@Composable
fun LearnMoreButton() {
    val mContext = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(
                    top = 50.dp,
                )
                .height(60.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(15, 82, 186),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 4.dp),
            onClick = {
                mContext.startActivity(Intent(mContext, SecondActivity::class.java))
            }
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxWidth(0.8f)) {
                val (startIcon, text, endIcon) = createRefs()
                Text(
                    fontSize = 23.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.constrainAs(text) {
                        start.linkTo(startIcon.end, margin = 8.dp)
                        bottom.linkTo(parent.bottom)
                        top.linkTo(parent.top)
                    }
                        .fillMaxWidth(),
                    text = "Learn more"
                )
            }
        }
    }
}

class SecondActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Calling the composable function
            // to display element and its contents
            MainContent2()
        }
    }
}

// Creating a composable
// function to display Top Bar
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainContent2() {
    Scaffold(
        topBar = { RecipesHeader() },
        content = { MyContent2() }
    )
}

@Composable
fun MyContent2(){
    Column(Modifier.fillMaxSize() .background(Color(43,39,30)), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        var content = "Listening device,  Hear-Hero makes "
        var content2 = "everyday sounds more accessible among "
        var content3 = "people who are hard of hearing."
        var content4 = "You can access Hear-Hero with these steps."
        var content5 = "1. Open your device's Settings app."
        var content6 = "2. Tap Accessibility, then click Sound Notifications."
        Text("Hear-Hero", fontSize = 35.sp, color = Color(15, 82, 186))
        Spacer(modifier = Modifier.height(40.dp))
        Text(content, fontSize = 18.sp, color = Color.White)
        Spacer(modifier = Modifier.height(2.dp))
        Text(content2, fontSize = 18.sp, color = Color.White)
        Spacer(modifier = Modifier.height(2.dp))
        Text(content3, fontSize = 18.sp, color = Color.White)
        Spacer(modifier = Modifier.height(50.dp))
        Text(content4, fontSize = 18.sp, color = Color.White)
        Spacer(modifier = Modifier.height(50.dp))
        Text(content5, fontSize = 16.sp, color = Color.White)
        Spacer(modifier = Modifier.height(10.dp))
        Text(content6, fontSize = 16.sp, color = Color.White)
    }
}

@Composable
fun RecipesHeader(
//    navController: NavController
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        val (image, info, topBar, title) = createRefs()
        AsyncImage(
            model = R.drawable.learn_more,
            requestBuilder = { crossfade(true) },
            contentDescription = "Cuisine image",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .constrainAs(image) {
                    linkTo(
                        start = parent.start,
                        top = parent.top,
                        end = parent.end,
                        bottom = info.top,
                        bottomMargin = (-32).dp
                    )
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                },
            contentScale = ContentScale.Crop
        )

        DetailsAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .constrainAs(topBar) {
                    linkTo(start = parent.start, end = parent.end)
                    top.linkTo(parent.top)
                    width = Dimension.fillToConstraints
                }
        ) { }
        Surface(
            modifier = Modifier
                .constrainAs(info) {
                    linkTo(start = parent.start, end = parent.end)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                }
        ) {
        }
    }
}

@Composable
fun DetailsAppBar(modifier: Modifier, onBackPressed: () -> Unit) {
    ConstraintLayout(modifier) {
        val (back, share) = createRefs()
        RecipeGradient(modifier = Modifier.fillMaxSize())
        IconButton(
            onClick = onBackPressed,
            Modifier.constrainAs(back) {
                start.linkTo(parent.start, margin = 8.dp)
                top.linkTo(parent.top, margin = 8.dp)
            }
        ) {
        }
        IconButton(
            onClick = { },
            Modifier.constrainAs(share) {
                end.linkTo(parent.end, margin = 8.dp)
                top.linkTo(parent.top, margin = 8.dp)
            }
        ) {
        }
    }
}

@Composable
fun RecipeGradient(modifier: Modifier) {
    Spacer(
        modifier = modifier.verticalGradient()
    )
}
