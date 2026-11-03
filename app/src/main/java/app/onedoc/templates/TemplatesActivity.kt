package app.onedoc.templates

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import app.onedoc.diagnostics.DiagnosticsLogger
import app.onedoc.editor.EditorActivity
import app.onedoc.model.Block
import app.onedoc.model.Document
import app.onedoc.repository.DocumentRepository
import app.onedoc.ui.theme.OneDocTheme

class TemplatesActivity : ComponentActivity() {

    private val repository: DocumentRepository by lazy {
        DocumentRepository.getInstance(this)
    }

    private val diagnostics by lazy {
        app.onedoc.diagnostics.DiagnosticsLogger(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diagnostics.recordNavigation("HomeActivity", "TemplatesActivity")

        setContent {
            OneDocTheme {
                TemplatesScreen(
                    context = this@TemplatesActivity,
                    repository = repository,
                    onTemplateSelected = { template ->
                        createDocumentFromTemplate(template)
                    },
                    onClose = { finish() },
                )
            }
        }
    }

    private fun createDocumentFromTemplate(template: Template) {
        val doc = Document(
            id = blockId(),
            title = template.title,
            blocks = template.blocks,
            version = 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        repository.createDocument(doc)
        diagnostics.recordDocumentCreated()
        diagnostics.debug("TEMPLATES", "Created document from template: ${template.title}")

        val intent = Intent(this, EditorActivity::class.java)
        intent.putExtra("documentId", doc.id)
        intent.putExtra("isNew", false)
        startActivity(intent)
        finish()
    }

    private fun blockId() = java.util.UUID.randomUUID().toString()
}

@Composable
fun TemplatesScreen(
    context: android.content.Context,
    repository: DocumentRepository,
    onTemplateSelected: (Template) -> Unit,
    onClose: () -> Unit,
) {
    val color = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            title = "Templates",
            onMenuClick = { onClose() },
            onSearchClick = { },
            modifier = Modifier,
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Choose a template",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        ) {
            items(items = BUILT_IN_TEMPLATES, key = { it.id }) { template ->
                TemplateCard(
                    template = template,
                    onClick = { onTemplateSelected(template) },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TemplateCard(template: Template, onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = color.surface,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = color.onSurfaceVariant,
                    maxLines = 2,
                )
            }
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "Template",
                tint = color.primary,
            )
        }
    }
}

data class Template(
    val id: String,
    val title: String,
    val description: String,
    val blocks: List<Block>,
)

val BUILT_IN_TEMPLATES: List<Template> = listOf(
    Template(
        id = "blank",
        title = "Blank",
        description = "Start with a clean page",
        blocks = listOf(Block.Paragraph(id = blockId(), text = "")),
    ),
    Template(
        id = "letter",
        title = "Letter",
        description = "Formal letter with heading and body",
        blocks = listOf(
            Block.Paragraph(id = blockId(), text = "[Your Name]\n[Address]\n[City, State ZIP]"),
            Block.Paragraph(id = blockId(), text = "[Date]"),
            Block.Paragraph(id = blockId(), text = "[Recipient Name]\n[Recipient Address]"),
            Block.Paragraph(id = blockId(), text = "Dear [Recipient],"),
            Block.Paragraph(id = blockId(), text = ""),
            Block.Paragraph(id = blockId(), text = "Sincerely,"),
            Block.Paragraph(id = blockId(), text = "[Your Name]"),
        ),
    ),
    Template(
        id = "meeting-notes",
        title = "Meeting Notes",
        description = "Agenda, attendees, action items",
        blocks = listOf(
            Block.Heading(id = blockId(), level = 1, text = "Meeting Notes"),
            Block.Paragraph(id = blockId(), text = "Date: ______"),
            Block.Paragraph(id = blockId(), text = "Attendees: ______"),
            Block.Heading(id = blockId(), level = 2, text = "Agenda"),
            Block.ListItem(id = blockId(), ordered = false, text = ""),
            Block.ListItem(id = blockId(), ordered = false, text = ""),
            Block.Heading(id = blockId(), level = 2, text = "Action Items"),
            Block.ListItem(id = blockId(), ordered = false, text = ""),
        ),
    ),
    Template(
        id = "essay",
        title = "Essay / Report",
        description = "Title, introduction, body, conclusion",
        blocks = listOf(
            Block.Title(id = blockId(), text = "Title"),
            Block.Paragraph(id = blockId(), text = "Introduction"),
            Block.Heading(id = blockId(), level = 2, text = "Body"),
            Block.Paragraph(id = blockId(), text = ""),
            Block.Heading(id = blockId(), level = 2, text = "Conclusion"),
            Block.Paragraph(id = blockId(), text = ""),
        ),
    ),
    Template(
        id = "invoice",
        title = "Invoice",
        description = "Billable items, totals, payment terms",
        blocks = listOf(
            Block.Title(id = blockId(), text = "INVOICE"),
            Block.Paragraph(id = blockId(), text = "Invoice #: ______    Date: ______"),
            Block.Paragraph(id = blockId(), text = "Bill To: ______"),
            Block.Table(id = blockId(), rows = 4, cols = 4, cells = listOf(
                listOf("Item", "Description", "Hours", "Rate"),
                listOf("", "", "", ""),
                listOf("", "", "", ""),
                listOf("Total", "", "", ""),
            )),
            Block.Paragraph(id = blockId(), text = "Payment terms: ______"),
        ),
    ),
    Template(
        id = "resume",
        title = "Resume / CV",
        description = "Summary, experience, education, skills",
        blocks = listOf(
            Block.Title(id = blockId(), text = "Full Name"),
            Block.Paragraph(id = blockId(), text = "Email | Phone | Location"),
            Block.Heading(id = blockId(), level = 2, text = "Professional Summary"),
            Block.Paragraph(id = blockId(), text = ""),
            Block.Heading(id = blockId(), level = 2, text = "Experience"),
            Block.ListItem(id = blockId(), ordered = false, text = ""),
            Block.Heading(id = blockId(), level = 2, text = "Education"),
            Block.ListItem(id = blockId(), ordered = false, text = ""),
            Block.Heading(id = blockId(), level = 2, text = "Skills"),
            Block.ListItem(id = blockId(), ordered = false, text = ""),
        ),
    ),
    Template(
        id = "weekly-plan",
        title = "Weekly Plan",
        description = "Monday through Sunday layout",
        blocks = listOf(
            Block.Title(id = blockId(), text = "Week of ______"),
            Block.ListItem(id = blockId(), ordered = false, text = "Monday:"),
            Block.ListItem(id = blockId(), ordered = false, text = "Tuesday:"),
            Block.ListItem(id = blockId(), ordered = false, text = "Wednesday:"),
            Block.ListItem(id = blockId(), ordered = false, text = "Thursday:"),
            Block.ListItem(id = blockId(), ordered = false, text = "Friday:"),
            Block.ListItem(id = blockId(), ordered = false, text = "Saturday:"),
            Block.ListItem(id = blockId(), ordered = false, text = "Sunday:"),
        ),
    ),
    Template(
        id = "project-brief",
        title = "Project Brief",
        description = "Overview, goals, timeline, stakeholders",
        blocks = listOf(
            Block.Title(id = blockId(), text = "Project Brief"),
            Block.Paragraph(id = blockId(), text = "Project Name: ______"),
            Block.Paragraph(id = blockId(), text = "Objective:"),
            Block.Paragraph(id = blockId(), text = ""),
            Block.Paragraph(id = blockId(), text = "Key Results:"),
            Block.ListItem(id = blockId(), ordered = false, text = ""),
            Block.ListItem(id = blockId(), ordered = false, text = ""),
            Block.Paragraph(id = blockId(), text = "Timeline:"),
            Block.Paragraph(id = blockId(), text = ""),
        ),
    ),
    Template(
        id = "minutes",
        title = "Meeting Minutes",
        description = "Formal minutes with decisions and next steps",
        blocks = listOf(
            Block.Title(id = blockId(), text = "Meeting Minutes"),
            Block.Paragraph(id = blockId(), text = "Date: ______    Time: ______"),
            Block.Paragraph(id = blockId(), text = "Attendees:"),
            Block.ListItem(id = blockId(), ordered = false, text = ""),
            Block.Paragraph(id = blockId(), text = "Agenda Items:"),
            Block.ListItem(id = blockId(), ordered = false, text = ""),
            Block.Paragraph(id = blockId(), text = "Decisions:"),
            Block.ListItem(id = blockId(), ordered = false, text = ""),
            Block.Paragraph(id = blockId(), text = "Next Steps:"),
            Block.ListItem(id = blockId(), ordered = false, text = ""),
        ),
    ),
    Template(
        id = "recipe",
        title = "Recipe",
        description = "Ingredients, directions, notes",
        blocks = listOf(
            Block.Title(id = blockId(), text = "Recipe Name"),
            Block.Paragraph(id = blockId(), text = "Servings: ______    Prep time: ______"),
            Block.Heading(id = blockId(), level = 2, text = "Ingredients"),
            Block.ListItem(id = blockId(), ordered = false, text = ""),
            Block.ListItem(id = blockId(), ordered = false, text = ""),
            Block.Heading(id = blockId(), level = 2, text = "Directions"),
            Block.ListItem(id = blockId(), ordered = true, text = ""),
            Block.ListItem(id = blockId(), ordered = true, text = ""),
        ),
    ),
    Template(
        id = "reading-list",
        title = "Reading List",
        description = "Books to read with notes",
        blocks = listOf(
            Block.Title(id = blockId(), text = "Reading List"),
            Block.ListItem(id = blockId(), ordered = false, text = "Title: ______ — Author: ______"),
            Block.ListItem(id = blockId(), ordered = false, text = "Notes:"),
            Block.ListItem(id = blockId(), ordered = false, text = ""),
        ),
    ),
    Template(
        id = "thesis-outline",
        title = "Thesis Outline",
        description = "Abstract, chapters, bibliography",
        blocks = listOf(
            Block.Title(id = blockId(), text = "Thesis Outline"),
            Block.Paragraph(id = blockId(), text = "Abstract:"),
            Block.Paragraph(id = blockId(), text = ""),
            Block.Heading(id = blockId(), level = 1, text = "Chapter 1: Introduction"),
            Block.Paragraph(id = blockId(), text = ""),
            Block.Heading(id = blockId(), level = 1, text = "Chapter 2: Literature Review"),
            Block.Paragraph(id = blockId(), text = ""),
            Block.Paragraph(id = blockId(), text = "Bibliography:"),
            Block.ListItem(id = blockId(), ordered = false, text = ""),
        ),
    ),
)

private fun blockId() = java.util.UUID.randomUUID().toString()