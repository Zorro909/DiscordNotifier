package de.zorro909.discordnotifier.entity.component.select;

import io.micronaut.configuration.hibernate.jpa.proxy.GenerateProxy;
import io.micronaut.core.annotation.Introspected;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Introspected
@GenerateProxy
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SelectOption {

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private int orderIndex;

    @NotNull
    @Column(nullable = false, length = 100)
    private String label;

    @Nullable
    @Column(nullable = true, length = 100)
    private String optionValue;

    @Nullable
    @Column(nullable = true, length = 100)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean defaultSelected = false;

}
