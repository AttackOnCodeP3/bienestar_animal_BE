package com.project.demo.logic.entity.diagnostico_ia;
import com.project.demo.logic.entity.animal.Animal;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "diagnostico_ia")
public class DiagnosticoIa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "animal_id")
    private Animal animal;

    @Column(length = 250)
    private String descripcionUsuario;

    @Column(columnDefinition = "TEXT")
    private String diagnosticoIA;

    @Column(length = 500)
    private String imagenUrl;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
