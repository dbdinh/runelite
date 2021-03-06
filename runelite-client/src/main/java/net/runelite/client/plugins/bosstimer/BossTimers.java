/*
 * Copyright (c) 2016-2017, Cameron Moberg <Moberg@tuta.io>
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.bosstimer;

import com.google.common.eventbus.Subscribe;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Actor;
import net.runelite.client.events.ActorDeath;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BossTimers extends Plugin
{
	private static final Logger logger = LoggerFactory.getLogger(BossTimers.class);

	private final BossTimersOverlay overlay = new BossTimersOverlay(this, OverlayPosition.TOP_LEFT, OverlayPriority.LOW);

	private final List<Boss> bosses = loadBossData();
	private final List<RespawnTimer> timers = new ArrayList<>();

	@Override
	protected void startUp() throws Exception
	{
	}

	@Override
	protected void shutDown() throws Exception
	{
	}

	@Override
	public Overlay getOverlay()
	{
		return overlay;
	}

	public List<RespawnTimer> getTimers()
	{
		return timers;
	}

	@Subscribe
	public void onActorDeath(ActorDeath death)
	{
		Actor actor = death.getActor();

		Boss boss = findBoss(actor.getName());
		if (boss == null)
		{
			return;
		}

		if (findTimerFor(actor.getName()) != null)
		{
			return;
		}

		logger.debug("Creating spawn timer for {} ({} seconds)", actor.getName(), boss.getSpawnTime());

		Instant respawnTime = Instant.now().plus(boss.getSpawnTime(), ChronoUnit.SECONDS);
		RespawnTimer respawnTimer = new RespawnTimer(boss, respawnTime);

		timers.add(respawnTimer);
	}

	public Boss findBoss(String name)
	{
		for (Boss boss : bosses)
		{
			if (boss.getName().equals(name))
			{
				return boss;
			}
		}
		return null;
	}

	private RespawnTimer findTimerFor(String name)
	{
		for (RespawnTimer timer : timers)
		{
			if (timer.getBoss().getName().equals(name))
			{
				return timer;
			}
		}
		return null;
	}

	private static List<Boss> loadBossData()
	{
		Gson gson = new Gson();
		Type type = new TypeToken<List<Boss>>()
		{
		}.getType();

		InputStream in = BossTimers.class.getResourceAsStream("boss_timers.json");
		return gson.fromJson(new InputStreamReader(in), type);
	}

}
