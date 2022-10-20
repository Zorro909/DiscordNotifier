package de.zorro909.discordnotifier.config.mapper;

import de.zorro909.discordnotifier.api.SubmitNotificationDto;
import de.zorro909.discordnotifier.api.component.DiscordInteractionComponentDto;
import de.zorro909.discordnotifier.api.component.button.DiscordButtonComponentDto;
import de.zorro909.discordnotifier.api.component.select.DiscordSelectMenuComponentDto;
import de.zorro909.discordnotifier.api.component.select.SelectOptionDto;
import de.zorro909.discordnotifier.api.component.text.DiscordTextInputComponentDto;
import de.zorro909.discordnotifier.entity.DiscordNotification;
import de.zorro909.discordnotifier.entity.component.DiscordInteractionComponent;
import de.zorro909.discordnotifier.entity.component.button.DiscordButtonComponent;
import de.zorro909.discordnotifier.entity.component.select.DiscordSelectMenuComponent;
import de.zorro909.discordnotifier.entity.component.select.SelectOption;
import de.zorro909.discordnotifier.entity.component.text.DiscordTextInputComponent;
import org.jetbrains.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Mapper
public interface NotificationSubmissionMapper {

    NotificationSubmissionMapper INSTANCE = Mappers.getMapper(NotificationSubmissionMapper.class);

    @Mapping(target = "components", expression = "java(mapComponents(submitNotificationDto.getComponents()))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "messageId", ignore = true)
    @Mapping(target = "sent", ignore = true)
    @Mapping(target = "markedForDeletion", ignore = true)
    DiscordNotification mapToEntity(SubmitNotificationDto submitNotificationDto, String source);

    default List<DiscordInteractionComponent> mapComponents(
            @Nullable List<List<DiscordInteractionComponentDto>> components) {
        // Map the components to the entity and add row and column information
        if (components == null) {
            return new ArrayList<>();
        }

        return IntStream
                .range(0, components.size())
                .mapToObj(row -> IntStream
                        .range(0, components.get(row).size())
                        .mapToObj(column -> mapInteractionToEntity(components.get(row).get(column), row, column)))
                .reduce(Stream::concat)
                .orElseGet(Stream::empty)
                .toList();
    }

    default DiscordInteractionComponent mapInteractionToEntity(
            DiscordInteractionComponentDto discordInteractionComponentDto, int row, int column) {
        DiscordInteractionComponent entity = switch (discordInteractionComponentDto.getType()) {
            case BUTTON -> mapButtonToEntity((DiscordButtonComponentDto) discordInteractionComponentDto);
            case SELECT_MENU -> mapSelectMenuToEntity((DiscordSelectMenuComponentDto) discordInteractionComponentDto);
        };
        return entity.setRowIndex(row).setColumnIndex(column);
    }

    @Mapping(target = "modalTextInputs", expression = "java(mapTextInputs(discordButtonComponentDto.getModalTextInputs()))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rowIndex", ignore = true)
    @Mapping(target = "columnIndex", ignore = true)
    @Mapping(target = "parent", ignore = true)
    DiscordButtonComponent mapButtonToEntity(DiscordButtonComponentDto discordButtonComponentDto);

    @Mapping(target = "options", expression = "java(mapOptions(discordSelectMenuComponentDto.getOptions()))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rowIndex", ignore = true)
    @Mapping(target = "columnIndex", ignore = true)
    @Mapping(target = "parent", ignore = true)
    DiscordSelectMenuComponent mapSelectMenuToEntity(DiscordSelectMenuComponentDto discordSelectMenuComponentDto);

    default List<DiscordTextInputComponent> mapTextInputs(@Nullable List<DiscordTextInputComponentDto> textInputs) {
        if (textInputs == null) {
            return new ArrayList<>();
        }

        return IntStream
                .range(0, textInputs.size())
                .mapToObj(index -> mapTextInputToEntity(textInputs.get(index), index))
                .toList();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderIndex", ignore = true)
    @Mapping(target = "parent", ignore = true)
    DiscordTextInputComponent mapTextInputToEntity(
            DiscordTextInputComponentDto discordTextInputComponentDto, int orderIndex);

    default List<SelectOption> mapOptions(SelectOptionDto[] options) {
        return IntStream
                .range(0, options.length)
                .mapToObj(order -> mapOptionToEntity(options[order]).setOrderIndex(order))
                .toList();
    }

    @Mapping(target = "defaultSelected", defaultValue = "false")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderIndex", ignore = true)
    SelectOption mapOptionToEntity(SelectOptionDto selectOptionDto);

}
