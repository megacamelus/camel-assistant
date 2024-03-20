package org.apache.camel.assistant.ui.vaadin;

import java.util.ArrayList;
import java.util.Collection;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Border;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;

import jakarta.inject.Inject;

@Route(value = "")
public class ChatView extends VerticalLayout {

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

        H2 welcomeMessage = new H2("Hello! How can I help you today?");
        welcomeMessage.addClassNames(FontSize.XXLARGE, FontWeight.LIGHT, TextColor.TERTIARY);

        HorizontalLayout welcomeContainer = new HorizontalLayout();
        welcomeContainer.setSizeFull();
        welcomeContainer.setPadding(true);
        welcomeContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        welcomeContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        welcomeContainer.add(welcomeMessage);

        Collection<MessageListItem> messageListItems = new ArrayList<MessageListItem>();
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

            } catch (Exception e) {
                e.printStackTrace();
                response = "I'm sorry. Something went wrong. Please check the logs.";
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
}
