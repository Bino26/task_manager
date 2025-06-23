package com.taskmanager.taskmanager.event;

import java.util.UUID;

public record ProprietaireDeletedEvent(
        UUID proprietaireId
) {}
