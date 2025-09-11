package com.serviconli.task.repository;

import com.serviconli.task.model.EstadoTarea;
import com.serviconli.task.model.Prioridad;
import com.serviconli.task.model.Tarea;
import com.serviconli.task.model.TipoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    // --- Búsquedas por VINCULACIÓN CON PACIENTE ---

    /**
     * Busca tareas por un número de identificación específico.
     * USADO PARA: Encontrar todas las tareas de un solo paciente.
     */
    List<Tarea> findByPacienteNumeroIdentificacion(String pacienteNumeroIdentificacion);

    /**
     * Busca tareas que pertenezcan a CUALQUIERA de los IDs en la lista.
     * USADO PARA: La búsqueda por nombre, después de obtener los IDs desde patient-service.
     */
    List<Tarea> findByPacienteNumeroIdentificacionIn(List<String> numerosIdentificacion);


    // --- Búsquedas por ATRIBUTOS DE TAREA ---

    List<Tarea> findByEstado(EstadoTarea estado);

    List<Tarea> findByPrioridad(Prioridad prioridad);

    /**
     * Busca tareas por el tipo de cita (la categoría general).
     * EJEMPLO: Traer todas las tareas que son de tipo "ESPECIALISTA".
     */
    List<Tarea> findByTipoCita(TipoCita tipoCita);

    /**
     * Busca tareas por el texto de la especialidad.
     * EJEMPLO: Traer todas las tareas de "Cardiología", sin importar su estado o tipo.
     */
    List<Tarea> findByEspecialidadContainingIgnoreCase(String especialidad);

}