package org.apache.camel.assistant.ui.vaadin;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import jakarta.inject.Inject;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Border;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;
import org.jboss.logging.Logger;

@Route(value = "")
public class ChatView extends VerticalLayout {

    private static final Logger LOG = Logger.getLogger(ChatView.class);

    @Inject
    ChatService chatService;

    public ChatView() {
        setSizeFull();
        setMaxWidth(800, Unit.PIXELS);
        addClassName(Margin.AUTO);

        H1 title = new H1("Camel Assistant");
        title.addClassNames(FontSize.XLARGE, FontWeight.LIGHT);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.addClassName(Border.BOTTOM);
        header.add(title);

        final HorizontalLayout welcomeContainer = createWelcomeContainer();

        Collection<MessageListItem> messageListItems = new ArrayList<>();
        MessageList messageList = new MessageList(messageListItems);
        messageList.setSizeFull();

        MessageInput messageInput = new MessageInput();
        messageInput.addSubmitListener(event -> {
            String text = event.getValue();
            if (text == null || text.isBlank()) {
                return;
            }
            
            if (messageList.getItems().isEmpty())
                remove(welcomeContainer);

            MessageListItem userMessage = new MessageListItem(text, null, "You");
            userMessage.setUserColorIndex(1);
            messageListItems.add(userMessage);

            String response = null;

            try {
                response = chatService.send(text);
            } catch (URISyntaxException e) {
                LOG.error("Invalid URI: {}", e.getMessage(), e);

                response = "I'm sorry. Something went wrong. Please check the logs.";
            } catch (IOException e) {
                LOG.error("I/O error while sending the message: {}", e.getMessage(), e);

                response = "I'm sorry. An I/O error occurred while sending the message. Please check the logs.";
            } catch (InterruptedException e) {
                LOG.error("Interrupted while sending the message: {}", e.getMessage(), e);

                response = "I'm sorry. The system was interrupted while sending the message. Please check the logs.";

                Thread.currentThread().interrupt();
            } finally {
                MessageListItem botMessage = new MessageListItem(response, null, "Assistant");
                botMessage.setUserColorIndex(2);
                messageListItems.add(botMessage);
            }
            
            messageList.setItems(messageListItems);
        });
        messageInput.setWidthFull();
        messageInput.addClassName(Padding.NONE);

        add(header, welcomeContainer, messageList, messageInput);
    }

    private static HorizontalLayout createWelcomeContainer() {
        H2 welcomeMessage = new H2("Hello! How can I help you today?");
        welcomeMessage.addClassNames(FontSize.XXLARGE, FontWeight.LIGHT, TextColor.TERTIARY);

        HorizontalLayout welcomeContainer = new HorizontalLayout();
        welcomeContainer.setSizeFull();
        welcomeContainer.setPadding(true);
        welcomeContainer.setAlignItems(Alignment.CENTER);
        welcomeContainer.setJustifyContentMode(JustifyContentMode.CENTER);
        welcomeContainer.add(welcomeMessage);
        return welcomeContainer;
    }
}
