// file: com/vecinapp/ui/screen/CommunitiesScreen.kt
package com.vecinapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.Timestamp
import com.vecinapp.domain.model.Community
import com.vecinapp.domain.model.DirectiveMember
import com.vecinapp.domain.model.Event
import com.vecinapp.domain.model.Membership
import com.vecinapp.domain.model.Proposal
import com.vecinapp.domain.model.Role
import com.vecinapp.domain.model.Status
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

// Pantalla principal de comunidades
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunitiesScreenWithMockData() {
    val communities = createMockCommunities()
    val currentUserId = "user123" // ID de usuario de ejemplo

    // Estado para controlar qué comunidad está seleccionada
    var selectedCommunity by remember { mutableStateOf<Community?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var filterDistance by remember { mutableStateOf(5) } // km

    // Determinar si el usuario es miembro de la comunidad seleccionada
    val isMember = selectedCommunity?.members?.any { it.userId == currentUserId } ?: false

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (selectedCommunity != null) {
            // Vista detallada de la comunidad
            CommunityDetailScreen(
                community = selectedCommunity!!,
                isMember = isMember,
                currentUserId = currentUserId,
                onBackClick = { selectedCommunity = null },
                onJoinClick = { /* Simular unirse */ },
                onLeaveClick = { /* Simular salir */ }
            )
        } else {
            // Lista de comunidades
            Scaffold(
                topBar = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Barra de búsqueda
                        val onActiveChange = { isSearchActive = it }
                        val colors1 =
                            SearchBarDefaults.colors()// Slider para la distancia (simplificado con chips)
                        // Sugerencias de búsqueda
                        SearchBar(
                            inputField = {
                                SearchBarDefaults.InputField(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    onSearch = { isSearchActive = false },
                                    expanded = isSearchActive,
                                    onExpandedChange = onActiveChange,
                                    enabled = true,
                                    placeholder = { Text("Buscar comunidades cercanas") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = "Buscar"
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { /* Abrir filtros */ }) {
                                            Icon(
                                                Icons.Default.FilterList,
                                                contentDescription = "Filtros"
                                            )
                                        }
                                    },
                                    colors = colors1.inputFieldColors,
                                    interactionSource = null,
                                )
                            },
                            expanded = isSearchActive,
                            onExpandedChange = onActiveChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = SearchBarDefaults.inputFieldShape,
                            colors = colors1,
                            tonalElevation = SearchBarDefaults.TonalElevation,
                            shadowElevation = SearchBarDefaults.ShadowElevation,
                            windowInsets = SearchBarDefaults.windowInsets,
                            content = fun ColumnScope.() {
                                // Sugerencias de búsqueda
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        "Filtrar por distancia: $filterDistance km",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Slider para la distancia (simplificado con chips)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        FilterDistanceChip(
                                            distance = 1,
                                            selected = filterDistance == 1
                                        ) {
                                            filterDistance = 1
                                        }
                                        FilterDistanceChip(
                                            distance = 5,
                                            selected = filterDistance == 5
                                        ) {
                                            filterDistance = 5
                                        }
                                        FilterDistanceChip(
                                            distance = 10,
                                            selected = filterDistance == 10
                                        ) {
                                            filterDistance = 10
                                        }
                                        FilterDistanceChip(
                                            distance = 20,
                                            selected = filterDistance == 20
                                        ) {
                                            filterDistance = 20
                                        }
                                    }
                                }
                            },
                        )

                        // Título de la sección
                        if (!isSearchActive) {
                            Text(
                                text = "Comunidades cercanas",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { /* Crear nueva comunidad */ },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Crear comunidad",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            ) { paddingValues ->
                // Lista de comunidades filtradas
                val filteredCommunities = communities.filter {
                    (it.name.contains(searchQuery, ignoreCase = true) ||
                            it.description?.contains(searchQuery, ignoreCase = true) == true) &&
                            getRandomDistance(it) <= filterDistance
                }

                LazyColumn(
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sección de comunidades a las que pertenece el usuario
                    val userCommunities = communities.filter {
                        it.members.any { member -> member.userId == currentUserId }
                    }

                    if (userCommunities.isNotEmpty()) {
                        item {
                            Text(
                                text = "Mis comunidades",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }

                        items(userCommunities) { community ->
                            CommunityCard(
                                community = community,
                                distance = getRandomDistance(community),
                                isMember = true,
                                onClick = { selectedCommunity = community }
                            )
                        }

                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    }

                    // Comunidades cercanas (excluyendo las del usuario)
                    val nearbyCommunities = filteredCommunities.filter {
                        !it.members.any { member -> member.userId == currentUserId }
                    }

                    if (nearbyCommunities.isNotEmpty()) {
                        item {
                            Text(
                                text = "Descubre comunidades cercanas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                            )
                        }

                        items(nearbyCommunities) { community ->
                            CommunityCard(
                                community = community,
                                distance = getRandomDistance(community),
                                isMember = false,
                                onClick = { selectedCommunity = community }
                            )
                        }
                    } else if (filteredCommunities.isEmpty()) {
                        item {
                            EmptyCommunitiesState()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterDistanceChip(
    distance: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text("$distance km") },
        leadingIcon = {
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    )
}

@Composable
private fun EmptyCommunitiesState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No se encontraron comunidades",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Intenta ampliar la distancia de búsqueda o crear tu propia comunidad",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CommunityCard(
    community: Community,
    distance: Double,
    isMember: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Imagen de la comunidad
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(community.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = community.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Overlay para mejorar legibilidad
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )

                // Distancia
                Card(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = String.format("%.1f km", distance),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Etiqueta de miembro
                if (isMember) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(14.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "Miembro",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                // Nombre de la comunidad
                Text(
                    text = community.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }

            // Contenido de la comunidad
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Dirección
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = community.address,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Descripción
                Text(
                    text = community.description ?: "Sin descripción",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Estadísticas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Miembros
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "${community.members.size} miembros",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Eventos
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "${community.events.size} eventos",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Propuestas
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HowToVote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "${community.proposals.size} propuestas",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommunityDetailScreen(
    community: Community,
    isMember: Boolean,
    currentUserId: String,
    onBackClick: () -> Unit,
    onJoinClick: () -> Unit,
    onLeaveClick: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Información", "Eventos", "Propuestas", "Directiva")

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Barra superior con botón de retroceso
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }

                    Text(
                        text = community.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    // Botón de notificaciones
                    if (isMember) {
                        IconButton(onClick = { /* Toggle notificaciones */ }) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notificaciones"
                            )
                        }
                    }

                    // Menú de opciones
                    var showMenu by remember { mutableStateOf(false) }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Más opciones"
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (isMember) {
                                DropdownMenuItem(
                                    text = { Text("Salir de la comunidad") },
                                    onClick = {
                                        onLeaveClick()
                                        showMenu = false
                                    }
                                )
                            }

                            DropdownMenuItem(
                                text = { Text("Reportar comunidad") },
                                onClick = {
                                    /* Reportar */
                                    showMenu = false
                                }
                            )
                        }
                    }
                }

                // Imagen de la comunidad
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(community.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = community.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Overlay para mejorar legibilidad
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                    )

                    // Botón de unirse/salir
                    if (!isMember) {
                        Button(
                            onClick = onJoinClick,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text("Unirse")
                        }
                    }

                    // Información básica
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = community.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = community.address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "${community.members.size} miembros",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }

                // Pestañas
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        // Contenido según la pestaña seleccionada
        LazyColumn(
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                when (selectedTabIndex) {
                    0 -> CommunityInfoTab(community, isMember)
                    1 -> CommunityEventsTab(community, isMember, currentUserId)
                    2 -> CommunityProposalsTab(community, isMember, currentUserId)
                    3 -> CommunityDirectiveTab(community, currentUserId)
                }
            }
        }
    }
}

@Composable
private fun CommunityInfoTab(
    community: Community,
    isMember: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Descripción
        Text(
            text = "Acerca de esta comunidad",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = community.description ?: "Sin descripción",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Estadísticas
        Text(
            text = "Estadísticas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatisticItem(
                icon = Icons.Default.Group,
                value = community.members.size.toString(),
                label = "Miembros"
            )

            StatisticItem(
                icon = Icons.Default.Event,
                value = community.events.size.toString(),
                label = "Eventos"
            )

            StatisticItem(
                icon = Icons.Default.HowToVote,
                value = community.proposals.size.toString(),
                label = "Propuestas"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reglas de la comunidad
        Text(
            text = "Reglas de la comunidad",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Reglas de ejemplo
        CommunityRule(
            number = 1,
            title = "Respeto mutuo",
            description = "Trata a todos los miembros con respeto y cortesía."
        )

        CommunityRule(
            number = 2,
            title = "Participación activa",
            description = "Se espera que los miembros participen en las actividades y votaciones."
        )

        CommunityRule(
            number = 3,
            title = "Contenido apropiado",
            description = "No se permite contenido ofensivo, discriminatorio o ilegal."
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Fecha de creación
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Comunidad creada el ${formatDate(community.createdAt?.toDate())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatisticItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CommunityRule(
    number: Int,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "$number.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(24.dp)
        )

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CommunityEventsTab(
    community: Community,
    isMember: Boolean,
    currentUserId: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Próximos eventos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Próximos eventos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (isMember) {
                TextButton(onClick = { /* Crear evento */ }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text("Crear evento")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filtros de eventos
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = true,
                onClick = { /* Filtrar todos */ },
                label = { Text("Todos") }
            )

            FilterChip(
                selected = false,
                onClick = { /* Filtrar asistiendo */ },
                label = { Text("Asistiré") }
            )

            FilterChip(
                selected = false,
                onClick = { /* Filtrar esta semana */ },
                label = { Text("Esta semana") }
            )

            FilterChip(
                selected = false,
                onClick = { /* Filtrar este mes */ },
                label = { Text("Este mes") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de eventos
        if (community.events.isEmpty()) {
            EmptyStateMessage(
                icon = Icons.Default.Event,
                title = "No hay eventos programados",
                message = "Sé el primero en crear un evento para esta comunidad"
            )
        } else {
            community.events.forEach { event ->
                EventListItem(
                    event = event,
                    currentUserId = currentUserId,
                    onClick = { /* Ver detalle del evento */ }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun EventListItem(
    event: Event,
    currentUserId: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fecha
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                val date = event.startDate?.toDate()
                val day = SimpleDateFormat("dd", Locale("es", "ES")).format(date)
                val month = SimpleDateFormat("MMM", Locale("es", "ES")).format(date)

                Text(
                    text = day,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = month.uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Detalles del evento
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    val time = SimpleDateFormat(
                        "HH:mm",
                        Locale("es", "ES")
                    ).format(event.startDate?.toDate())
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "${event.rsvps.count { it.status == com.vecinapp.domain.model.RsvpStatus.GOING }} asistentes",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Botón de asistencia
            val isGoing =
                event.rsvps.any { it.userId == currentUserId && it.status == com.vecinapp.domain.model.RsvpStatus.GOING }

            IconButton(
                onClick = { /* Toggle asistencia */ }
            ) {
                Icon(
                    imageVector = if (isGoing) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isGoing) "No asistiré" else "Asistiré",
                    tint = if (isGoing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CommunityProposalsTab(
    community: Community,
    isMember: Boolean,
    currentUserId: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Propuestas activas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Propuestas y votaciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (isMember) {
                TextButton(onClick = { /* Crear propuesta */ }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text("Nueva propuesta")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de propuestas
        if (community.proposals.isEmpty()) {
            EmptyStateMessage(
                icon = Icons.Default.HowToVote,
                title = "No hay propuestas activas",
                message = "Crea una propuesta para mejorar tu comunidad"
            )
        } else {
            community.proposals.forEach { proposal ->
                ProposalListItem(
                    proposal = proposal,
                    currentUserId = currentUserId,
                    isMember = isMember,
                    onClick = { /* Ver detalle de la propuesta */ }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProposalListItem(
    proposal: Proposal,
    currentUserId: String,
    isMember: Boolean,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Estado de la propuesta
                Chip(
                    onClick = { },
                    colors = ChipDefaults.chipColors(
                        contentColor = when (proposal.status) {
                            Status.PENDING -> MaterialTheme.colorScheme.primaryContainer
                            Status.APPROVED -> MaterialTheme.colorScheme.tertiaryContainer
                            Status.REJECTED -> MaterialTheme.colorScheme.errorContainer
                        }
                    ),
                    label = {
                        Text(
                            text = when (proposal.status) {
                                Status.PENDING -> "En votación"
                                Status.APPROVED -> "Aprobada"
                                Status.REJECTED -> "Rechazada"
                            }
                        )
                    }
                )

                // Fecha de expiración
                if (proposal.status == Status.PENDING) {
                    Text(
                        text = "Finaliza: ${formatDate(proposal.expiresAt?.toDate())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Título y descripción
            Text(
                text = proposal.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = proposal.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = if (expanded) TextOverflow.Visible else TextOverflow.Ellipsis
            )

            if (proposal.description.length > 100) {
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(if (expanded) "Ver menos" else "Ver más")

                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            // Progreso de la votación
            if (proposal.status == Status.PENDING) {
                Spacer(modifier = Modifier.height(8.dp))

                val votesInFavor = proposal.votes.count { it.approve }
                val totalVotes = proposal.votes.size
                val progress = if (totalVotes > 0) votesInFavor.toFloat() / totalVotes else 0f

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "A favor: $votesInFavor",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "Total: $totalVotes votos",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // Botones de votación
                if (isMember) {
                    Spacer(modifier = Modifier.height(16.dp))

                    val hasVoted = proposal.votes.any { it.userId == currentUserId }
                    val votedInFavor =
                        proposal.votes.any { it.userId == currentUserId && it.approve }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Votar a favor */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (hasVoted && votedInFavor)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = null,
                                tint = if (hasVoted && votedInFavor)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text("A favor")
                        }

                        OutlinedButton(
                            onClick = { /* Votar en contra */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (hasVoted && !votedInFavor)
                                    MaterialTheme.colorScheme.errorContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = if (hasVoted && !votedInFavor)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text("En contra")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityDirectiveTab(
    community: Community,
    currentUserId: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Directiva de la comunidad",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de miembros de la directiva
        if (community.directive.isEmpty()) {
            EmptyStateMessage(
                icon = Icons.Default.Group,
                title = "No hay directiva establecida",
                message = "La comunidad aún no ha designado una directiva"
            )
        } else {
            community.directive.forEach { directiveMember ->
                DirectiveMemberItem(
                    directiveMember = directiveMember,
                    isCurrentUser = directiveMember.userId == currentUserId
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Miembros regulares
        Text(
            text = "Miembros de la comunidad",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar algunos miembros (limitado a 5 para el ejemplo)
        community.members
            .filter { member -> community.directive.none { it.userId == member.userId } }
            .take(5)
            .forEach { member ->
                MemberItem(
                    membership = member,
                    isCurrentUser = member.userId == currentUserId
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

        // Botón para ver todos los miembros
        if (community.members.size > 5) {
            TextButton(
                onClick = { /* Ver todos los miembros */ },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Ver todos los miembros (${community.members.size})")

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun DirectiveMemberItem(
    directiveMember: DirectiveMember,
    isCurrentUser: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del miembro
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Usuario ${directiveMember.userId.takeLast(4)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rol
                    Chip(
                        onClick = { },
                        colors = ChipDefaults.chipColors(
                            contentColor = when (directiveMember.role) {
                                Role.ADMIN -> MaterialTheme.colorScheme.primaryContainer
                                Role.DIRECTIVE -> MaterialTheme.colorScheme.secondaryContainer
                                Role.MODERATOR -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        label = {
                            Text(
                                text = when (directiveMember.role) {
                                    Role.ADMIN -> "Administrador"
                                    Role.DIRECTIVE -> "Directivo"
                                    Role.MODERATOR -> "Moderador"
                                    else -> "Miembro"
                                }
                            )
                        }
                    )

                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "(Tú)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Fecha de nombramiento
            Text(
                text = "Desde ${formatDate(directiveMember.appointedAt?.toDate())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MemberItem(
    membership: Membership,
    isCurrentUser: Boolean
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del miembro
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Usuario ${membership.userId.takeLast(4)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "(Tú)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "Miembro desde ${formatDate(membership.joinedAt?.toDate())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(
    icon: ImageVector,
    title: String,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Funciones de utilidad

private fun formatDate(date: Date?): String {
    if (date == null) return "Fecha desconocida"
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
    return formatter.format(date)
}

// Función para generar una distancia aleatoria para cada comunidad
private fun getRandomDistance(community: Community): Double {
    // Usar el hash del ID de la comunidad para generar un número consistente
    val hash = community.id.hashCode()
    val random = Random(hash)
    return (random.nextDouble() * 10.0).coerceAtMost(20.0)
}

// Función para crear comunidades de ejemplo
private fun createMockCommunities(): List<Community> {
    val calendar = Calendar.getInstance()
    val now = Date()

    // Comunidad 1: Edificio Alameda
    val community1 = Community(
        id = "community1",
        name = "Edificio Alameda",
        description = "Comunidad de vecinos del Edificio Alameda. Compartimos espacios comunes y organizamos eventos para mejorar la convivencia.",
        creatorId = "user100",
        createdAt = Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 365)), // Hace 1 año
        updatedAt = Timestamp(now),
        members = listOf(
            Membership(
                "user100",
                Role.ADMIN,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 365))
            ),
            Membership(
                "user101",
                Role.MEMBER,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 300))
            ),
            Membership(
                "user102",
                Role.MEMBER,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 250))
            ),
            Membership(
                "user103",
                Role.MEMBER,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 200))
            ),
            Membership(
                "user123",
                Role.MEMBER,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 150))
            ) // Usuario actual
        ),
        directive = listOf(
            DirectiveMember(
                "user100",
                Role.ADMIN,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 365))
            ),
            DirectiveMember(
                "user101",
                Role.DIRECTIVE,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 300))
            ),
            DirectiveMember(
                "user123",
                Role.MODERATOR,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 100))
            ) // Usuario actual
        ),
        events = createMockEvents(3),
        proposals = createMockProposals(2),
        address = "Av. Alameda 123, Santiago",
        isPublic = true,
        imageUrl = "https://images.unsplash.com/photo-1560518883-ce09059eeffa?q=80&w=2573&auto=format&fit=crop"
    )

    // Comunidad 2: Condominio Las Palmas
    val community2 = Community(
        id = "community2",
        name = "Condominio Las Palmas",
        description = "Comunidad del Condominio Las Palmas. Un espacio para coordinar actividades, compartir información y mejorar nuestra calidad de vida.",
        creatorId = "user200",
        createdAt = Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 180)), // Hace 180 días
        updatedAt = Timestamp(now),
        members = listOf(
            Membership(
                "user200",
                Role.ADMIN,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 180))
            ),
            Membership(
                "user201",
                Role.MEMBER,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 150))
            ),
            Membership(
                "user202",
                Role.MEMBER,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 120))
            )
        ),
        directive = listOf(
            DirectiveMember(
                "user200",
                Role.ADMIN,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 180))
            ),
            DirectiveMember(
                "user201",
                Role.DIRECTIVE,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 150))
            )
        ),
        events = createMockEvents(2),
        proposals = createMockProposals(3),
        address = "Calle Las Palmas 456, Viña del Mar",
        isPublic = true,
        imageUrl = "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?q=80&w=2835&auto=format&fit=crop"
    )

    // Comunidad 3: Barrio El Bosque
    val community3 = Community(
        id = "community3",
        name = "Barrio El Bosque",
        description = "Vecinos del Barrio El Bosque unidos para mejorar nuestro entorno, organizar actividades culturales y mantener la seguridad de nuestra zona.",
        creatorId = "user300",
        createdAt = Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 90)), // Hace 90 días
        updatedAt = Timestamp(now),
        members = listOf(
            Membership("user300", Role.ADMIN, Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 90))),
            Membership(
                "user301",
                Role.MEMBER,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 85))
            ),
            Membership(
                "user302",
                Role.MEMBER,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 80))
            ),
            Membership(
                "user303",
                Role.MEMBER,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 75))
            ),
            Membership(
                "user304",
                Role.MEMBER,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 70))
            ),
            Membership("user305", Role.MEMBER, Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 65)))
        ),
        directive = listOf(
            DirectiveMember(
                "user300",
                Role.ADMIN,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 90))
            ),
            DirectiveMember(
                "user301",
                Role.DIRECTIVE,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 85))
            ),
            DirectiveMember(
                "user302",
                Role.MODERATOR,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 80))
            )
        ),
        events = createMockEvents(4),
        proposals = createMockProposals(1),
        address = "Av. Principal 789, El Bosque",
        isPublic = false,
        imageUrl = "https://images.unsplash.com/photo-1625602812206-5ec545ca1231?q=80&w=2670&auto=format&fit=crop"
    )

    // Comunidad 4: Plaza Central
    val community4 = Community(
        id = "community4",
        name = "Plaza Central",
        description = "Comunidad de vecinos alrededor de la Plaza Central. Nos organizamos para mantener limpia nuestra plaza, organizar ferias y eventos culturales.",
        creatorId = "user400",
        createdAt = Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 45)), // Hace 45 días
        updatedAt = Timestamp(now),
        members = listOf(
            Membership("user400", Role.ADMIN, Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 45))),
            Membership(
                "user401",
                Role.MEMBER,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 40))
            ),
            Membership("user402", Role.MEMBER, Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 35)))
        ),
        directive = listOf(
            DirectiveMember(
                "user400",
                Role.ADMIN,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 45))
            )
        ),
        events = createMockEvents(1),
        proposals = createMockProposals(0),
        address = "Plaza Central s/n, Concepción",
        isPublic = true,
        imageUrl = "https://images.unsplash.com/photo-1444653389962-8149286c578a?q=80&w=2574&auto=format&fit=crop"
    )

    // Comunidad 5: Edificio Mirador
    val community5 = Community(
        id = "community5",
        name = "Edificio Mirador",
        description = "Comunidad del Edificio Mirador. Un espacio para coordinar el uso de áreas comunes, organizar reuniones y compartir información relevante.",
        creatorId = "user500",
        createdAt = Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 30)), // Hace 30 días
        updatedAt = Timestamp(now),
        members = listOf(
            Membership("user500", Role.ADMIN, Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 30))),
            Membership(
                "user501",
                Role.MEMBER,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 25))
            ),
            Membership(
                "user502",
                Role.MEMBER,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 20))
            ),
            Membership("user503", Role.MEMBER, Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 15)))
        ),
        directive = listOf(
            DirectiveMember(
                "user500",
                Role.ADMIN,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 30))
            ),
            DirectiveMember(
                "user501",
                Role.DIRECTIVE,
                Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 25))
            )
        ),
        events = createMockEvents(2),
        proposals = createMockProposals(2),
        address = "Calle Vista 234, Valparaíso",
        isPublic = true,
        imageUrl = "https://images.unsplash.com/photo-1460317442991-0ec209397118?q=80&w=2670&auto=format&fit=crop"
    )

    return listOf(community1, community2, community3, community4, community5)
}

// Función para crear eventos de ejemplo
private fun createMockEvents(count: Int): List<Event> {
    val calendar = Calendar.getInstance()
    val now = Date()
    val events = mutableListOf<Event>()

    val eventTitles = listOf(
        "Reunión de vecinos",
        "Limpieza comunitaria",
        "Fiesta de vecinos",
        "Taller de jardinería",
        "Asamblea general",
        "Actividad deportiva",
        "Cine al aire libre",
        "Feria de artesanía"
    )

    val eventDescriptions = listOf(
        "Reunión para discutir temas importantes de la comunidad y planificar actividades futuras.",
        "Jornada de limpieza en áreas comunes. Trae guantes y bolsas de basura.",
        "Celebración anual de la comunidad con comida, música y actividades para todas las edades.",
        "Aprende a cuidar tus plantas y contribuye al embellecimiento de nuestra comunidad.",
        "Asamblea para elegir nueva directiva y aprobar presupuesto anual.",
        "Torneo deportivo para todas las edades. Inscríbete y participa.",
        "Proyección de películas al aire libre. Trae tu manta y disfruta.",
        "Exposición y venta de productos artesanales de miembros de la comunidad."
    )

    for (i in 0 until count) {
        // Algunos eventos en el futuro, otros en el pasado
        val daysOffset = if (i % 2 == 0) (i + 1) * 3 else -(i + 1) * 2
        calendar.time = now
        calendar.add(Calendar.DAY_OF_MONTH, daysOffset)
        calendar.set(Calendar.HOUR_OF_DAY, 18 + (i % 3))
        calendar.set(Calendar.MINUTE, 0)

        val startDate = calendar.time
        calendar.add(Calendar.HOUR_OF_DAY, 2)
        val endDate = calendar.time

        val titleIndex = i % eventTitles.size
        val descriptionIndex = i % eventDescriptions.size

        events.add(
            Event(
                id = "event$i",
                title = eventTitles[titleIndex],
                description = eventDescriptions[descriptionIndex],
                dateTime = Timestamp(startDate),
                location = null,
                imageUrl = "https://images.unsplash.com/photo-1511795409834-ef04bbd61622?q=80&w=2669&auto=format&fit=crop",
                organizerId = "user${100 + i}",
                createdAt = Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 7)), // Creado hace una semana
                updatedAt = Timestamp(now),
                attachments = emptyList(),
                comments = emptyList(),
                rsvps = createMockRsvps(5 + i),
                reports = emptyList(),
                voteCount = (5..15).random(),
                attendees = emptyList(),
                startDate = Timestamp(startDate),
                endDate = Timestamp(endDate)
            )
        )
    }

    return events
}

// Función para crear RSVPs de ejemplo
private fun createMockRsvps(count: Int): List<com.vecinapp.domain.model.Rsvp> {
    val rsvps = mutableListOf<com.vecinapp.domain.model.Rsvp>()
    val now = Date()

    // Asegurar que el usuario actual tenga un RSVP
    rsvps.add(
        com.vecinapp.domain.model.Rsvp(
            userId = "user123",
            status = com.vecinapp.domain.model.RsvpStatus.GOING,
            respondedAt = Timestamp(now)
        )
    )

    // Añadir otros RSVPs
    for (i in 1 until count) {
        val status = when (i % 3) {
            0 -> com.vecinapp.domain.model.RsvpStatus.GOING
            1 -> com.vecinapp.domain.model.RsvpStatus.MAYBE
            else -> com.vecinapp.domain.model.RsvpStatus.DECLINED
        }

        rsvps.add(
            com.vecinapp.domain.model.Rsvp(
                userId = "user${200 + i}",
                status = status,
                respondedAt = Timestamp(Date(now.time - 1000 * 60 * 60 * i)) // Cada uno respondió en un momento diferente
            )
        )
    }

    return rsvps
}

// Función para crear propuestas de ejemplo
private fun createMockProposals(count: Int): List<Proposal> {
    val proposals = mutableListOf<Proposal>()
    val now = Date()
    val calendar = Calendar.getInstance()

    val proposalTitles = listOf(
        "Instalación de cámaras de seguridad",
        "Renovación de áreas verdes",
        "Cambio de horario de reuniones",
        "Organización de evento comunitario",
        "Mejora del sistema de reciclaje",
        "Pintura de áreas comunes"
    )

    val proposalDescriptions = listOf(
        "Propuesta para instalar cámaras de seguridad en las entradas principales y áreas comunes para mejorar la seguridad de todos los vecinos.",
        "Renovación de las áreas verdes con plantas nativas que requieran menos agua y mantenimiento, contribuyendo a la sostenibilidad de nuestra comunidad.",
        "Cambiar el horario de las reuniones mensuales de las 19:00 a las 20:00 horas para permitir que más vecinos puedan asistir después del trabajo.",
        "Organizar un evento comunitario trimestral para fortalecer los lazos entre vecinos, con actividades para todas las edades y comida compartida.",
        "Implementar un sistema de reciclaje más eficiente con contenedores separados para diferentes materiales y educación sobre cómo reciclar correctamente.",
        "Pintar las áreas comunes con colores más modernos y alegres para mejorar el ambiente y la apariencia de nuestro edificio."
    )

    for (i in 0 until count) {
        // Fecha de expiración (algunas ya expiradas, otras vigentes)
        calendar.time = now
        val expirationOffset = if (i % 2 == 0) 10 else -5
        calendar.add(Calendar.DAY_OF_MONTH, expirationOffset)

        val status = if (expirationOffset < 0) {
            if (i % 2 == 0) Status.APPROVED else Status.REJECTED
        } else {
            Status.PENDING
        }

        val titleIndex = i % proposalTitles.size
        val descriptionIndex = i % proposalDescriptions.size

        // Crear votos
        val votes = mutableListOf<com.vecinapp.domain.model.Vote>()
        val voteCount = (3..8).random()

        // Asegurar que el usuario actual tenga un voto si la propuesta está pendiente
        if (status == Status.PENDING) {
            votes.add(
                com.vecinapp.domain.model.Vote(
                    userId = "user123",
                    votedAt = Timestamp(Date(now.time - 1000 * 60 * 60 * 24)),
                    approve = i % 2 == 0 // Alternar entre aprobar y rechazar
                )
            )
        }

        // Añadir otros votos
        for (j in 1 until voteCount) {
            votes.add(
                com.vecinapp.domain.model.Vote(
                    userId = "user${300 + j}",
                    votedAt = Timestamp(Date(now.time - 1000 * 60 * 60 * (24 - j))),
                    approve = j % 3 != 0 // Mayoría a favor
                )
            )
        }

        proposals.add(
            Proposal(
                id = "proposal$i",
                title = proposalTitles[titleIndex],
                description = proposalDescriptions[descriptionIndex],
                proposerId = "user${100 + i}",
                createdAt = Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 15)), // Creada hace 15 días
                updatedAt = Timestamp(now),
                attachments = emptyList(),
                comments = emptyList(),
                votes = votes,
                reports = emptyList(),
                voteCount = votes.size,
                expiresAt = Timestamp(calendar.time),
                status = status
            )
        )
    }

    return proposals
}

@Preview(showBackground = true)
@Composable
fun CommunitiesScreenPreview() {
    MaterialTheme {
        CommunitiesScreenWithMockData()
    }
}