package com.barcelo.businessrules.dynamicpack.decision.impl;

import org.drools.event.rule.ObjectInsertedEvent;
import org.drools.event.rule.ObjectRetractedEvent;
import org.drools.event.rule.ObjectUpdatedEvent;
import org.drools.event.rule.WorkingMemoryEventListener;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dag-vsf
 */
@Slf4j
public class PkgWorkingMemoryEventListener implements WorkingMemoryEventListener {
	public void objectInserted(ObjectInsertedEvent event) {
		log.info("Object inserted : {}.", event.getObject());
	}

	public void objectUpdated(ObjectUpdatedEvent event) {
		log.info("Object updated : {}.", event.getObject().getClass().getName());
	}

	public void objectRetracted(ObjectRetractedEvent event) {
		log.info("Object deleted : {}", event.getOldObject().getClass().getName());
	}
}
