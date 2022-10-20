package de.zorro909.discordnotifier.job;

import de.zorro909.discordnotifier.config.User;
import de.zorro909.discordnotifier.config.UserConfig;
import de.zorro909.discordnotifier.entity.DiscordNotification;
import de.zorro909.discordnotifier.entity.component.button.DiscordButtonComponent;
import de.zorro909.discordnotifier.entity.component.select.DiscordSelectMenuComponent;
import de.zorro909.discordnotifier.entity.component.text.DiscordTextInputComponent;
import de.zorro909.discordnotifier.entity.repository.DiscordNotificationRepository;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.utils.messages.AbstractMessageBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@AllArgsConstructor
@Slf4j
public class NotificationSendJob {

    private final DiscordNotificationRepository discordNotificationRepository;
    private final JDA                           jda;
    private final UserConfig                    userConfig;

    @Transactional
    public void sendNotifications() {
        discordNotificationRepository
                .findBySentFalse()
                .stream()
                .filter(notification -> !notification.isMarkedForDeletion())
                .map(this::sendMessage)
                .reduce(CompletableFuture::allOf)
                .orElseGet(() -> CompletableFuture.completedFuture(null))
                .join();
    }

    private CompletableFuture<Void> sendMessage(DiscordNotification discordNotification) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(discordNotification.getTitle());
        embedBuilder.setDescription(discordNotification.getMessage());
        embedBuilder.setImage(discordNotification.getImageUrl());
        embedBuilder.setAuthor(discordNotification.getSource());

        MessageEmbed embed = embedBuilder.build();

        AbstractMessageBuilder messageBuilder = new MessageCreateBuilder();
        if (discordNotification.getMessageId() != null) {
            messageBuilder = new MessageEditBuilder();
        }

        long id = discordNotification.getId();
        Stream<List<ActionComponent>> actionComponents =
                discordNotification
                        .getComponents()
                        .stream()
                        .map(interactionComponent -> {
                            ActionComponent component = switch (interactionComponent.getType()) {
                                case BUTTON -> createButton(id, (DiscordButtonComponent) interactionComponent);
                                case SELECT_MENU ->
                                        createSelectMenu(id, (DiscordSelectMenuComponent) interactionComponent);
                            };
                            return Pair.of(interactionComponent.getRowIndex(), component);
                        })
                        .collect(Collectors.groupingBy(Pair::getLeft))
                        .values()
                        .stream()
                        .map(list -> list.stream().map(Pair::getRight).toList());

        messageBuilder.setEmbeds(embed);

        Optional<String> channelId = userConfig
                .getUser(discordNotification.getSource())
                .map(User::getChannelIds)
                .map(channels -> channels.get(discordNotification.getChannelName()));
        Optional<TextChannel> textChannel = channelId.map(jda::getTextChannelById);

        if (channelId.isEmpty()) {
            log.error("No Channel Id is configured for the channel name {} of user {}",
                      discordNotification.getChannelName(), discordNotification.getSource());
            return CompletableFuture.failedFuture(null);
        }
        if (textChannel.isEmpty()) {
            log.error("Channel with id {} not found", channelId.get());
            return CompletableFuture.failedFuture(null);
        }

        val channel = textChannel.get();

        CompletableFuture<Message> messageFuture = null;

        if (messageBuilder instanceof MessageCreateBuilder messageCreateBuilder) {
            actionComponents.forEach(messageCreateBuilder::addActionRow);
            messageFuture = channel.sendMessage(messageCreateBuilder.build()).submit();
        } else {
            MessageEditBuilder messageEditBuilder = (MessageEditBuilder) messageBuilder;
            var actionRows = actionComponents.map(ActionRow::of).toList();
            messageEditBuilder.setComponents(actionRows);
            messageFuture =
                    channel.editMessageById(discordNotification.getMessageId(), messageEditBuilder.build()).submit();
        }

        return messageFuture.thenAccept((message) -> {
            discordNotification.setSent(true);
            discordNotification.setMessageId(message.getIdLong());
            discordNotificationRepository.update(discordNotification);
            log.info("Sent notification '{}' with id {}", discordNotification.getMessage(),
                     discordNotification.getId());
        }).exceptionally((throwable) -> {
            log.error("Error sending message", throwable);
            return null;
        });
    }

    private Button createButton(long id, DiscordButtonComponent buttonComponent) {
        ButtonStyle buttonStyle = switch (buttonComponent.getButtonStyle()) {
            case PRIMARY -> ButtonStyle.PRIMARY;
            case SECONDARY -> ButtonStyle.SECONDARY;
            case SUCCESS -> ButtonStyle.SUCCESS;
            case DANGER -> ButtonStyle.DANGER;
            case LINK -> ButtonStyle.LINK;
        };

        return Button.of(buttonStyle, buttonStyle == ButtonStyle.LINK ? Objects.requireNonNull(
                                 buttonComponent.getLinkUrl()) : id + "-" + buttonComponent.getInteractionId(),
                         buttonComponent.getLabel());
    }

    private SelectMenu createSelectMenu(long id, DiscordSelectMenuComponent selectMenuComponent) {
        SelectMenu.Builder menu = SelectMenu.create(id + "-" + selectMenuComponent.getInteractionId());

        selectMenuComponent
                .getOptions()
                .stream()
                .map(option -> SelectOption
                        .of(option.getLabel(), Objects.requireNonNull(option.getOptionValue()))
                        .withDescription(option.getDescription())
                        .withDefault(option.isDefaultSelected()))
                .forEach(menu::addOptions);

        return menu.build();
    }

    private TextInput createTextInput(long id, DiscordTextInputComponent textInputComponent) {
        return TextInput
                .create(id + "-" + textInputComponent.getInteractionId(), textInputComponent.getLabel(),
                        textInputComponent.isMultiline() ? TextInputStyle.PARAGRAPH : TextInputStyle.SHORT)
                .setPlaceholder(textInputComponent.getPlaceholder())
                .setMinLength(textInputComponent.getMinLength())
                .setMaxLength(textInputComponent.getMaxLength())
                .setRequired(textInputComponent.isRequired())
                .build();
    }

}
