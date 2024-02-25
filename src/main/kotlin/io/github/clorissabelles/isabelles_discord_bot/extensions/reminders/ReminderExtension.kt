package io.github.clorissabelles.isabelles_discord_bot.extensions.reminders

import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import io.github.clorissabelles.isabelles_discord_bot.EARLY_REFRESH_HOUR
import io.github.clorissabelles.isabelles_discord_bot.LATE_REFRESH_HOUR
import io.github.clorissabelles.isabelles_discord_bot.OWNER_SNOWFLAKE
import io.github.clorissabelles.isabelles_discord_bot.config.*
import io.github.clorissabelles.isabelles_discord_bot.extensions.reminders.messages.*
import io.github.clorissabelles.isabelles_discord_bot.getCurrentTime
import io.github.ellie_semele.ellie_utility_bot.extensions.reminders.messages.*
import io.github.clorissabelles.isabelles_discord_bot.extensions.reminders.messages.AntiAndrogenMessage.Companion.createAntiAndrogenMessage
import io.github.ellie_semele.ellie_utility_bot.extensions.reminders.messages.CourseraMessage.Companion.createCourseraMessage
import io.github.clorissabelles.isabelles_discord_bot.extensions.reminders.messages.EstrogenMessage.Companion.createEstrogenMessage
import io.github.clorissabelles.isabelles_discord_bot.extensions.reminders.messages.MedicineMessage.Companion.createMedicineMessage
import io.github.clorissabelles.isabelles_discord_bot.extensions.reminders.messages.TeethBrushingMessage.Companion.createToothBrushingMessage
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.days

class ReminderExtension : Extension() {
	override val name: String = "reminder_extension"

	private val executor = ScheduledThreadPoolExecutor(1)

	private var teethBrushingMessage: TeethBrushingMessage? = null
	private var courseraMessage: CourseraMessage? = null
	private var estrogenMessage: EstrogenMessage? = null
	private var antiAndrogenMessage: AntiAndrogenMessage? = null
	private var medicineMessage: MedicineMessage? = null

	override suspend fun setup() {
		ephemeralSlashCommand {
			name = "reminder"
			description = "Commands for creating reminders."

			ephemeralSubCommand {
				name = "create-tooth-brushing-reminder"
				description = "Creates the tooth brushing reminder."

				action {
					teethBrushingMessage = createToothBrushingMessage(this@ReminderExtension)
				}
			}

			ephemeralSubCommand(::CourseraReminderArgs) {
				name = "create-coursera-reminder"
				description = "Creates the coursera reminder."

				action {
					courseraMessage = createCourseraMessage(this@ReminderExtension)
				}
			}

			ephemeralSubCommand(::EstrogenReminderArgs) {
				name = "create-estrogen-reminder"
				description = "Creates the estrogen reminder."

				action {
					estrogenMessage = createEstrogenMessage(this@ReminderExtension)
				}
			}

			ephemeralSubCommand {
				name = "create-anti-androgen-reminder"
				description = "Creates the anti-androgen reminder."

				action {
					antiAndrogenMessage = createAntiAndrogenMessage(this@ReminderExtension)
				}
			}

			ephemeralSubCommand(::MedicineReminderArgs) {
				name = "create-medicine-reminder"
				description = "Creates the medicine reminder."

				action {
					medicineMessage = createMedicineMessage(this@ReminderExtension)
				}
			}

			ephemeralSubCommand {
				name = "refresh"
				description = "Refresh the created reminders."

				action {
					refreshReminders()

					respond { content = "Refreshing reminders in 10 seconds." }
				}
			}
		}

		event<ButtonInteractionCreateEvent> {
			check {
				failIfNot(
					event.interaction.user.id == OWNER_SNOWFLAKE,
					"Must be <@${OWNER_SNOWFLAKE}> to use this button."
				)
			}

			action {
				val componentId = event.interaction.componentId

				when (componentId) {
					TeethBrushingMessage.MORNING_BUTTON_ID -> {
						teethBrushingMessage?.completeMorning()

						ConfigHandler.save()

						event.interaction.deferPublicMessageUpdate()
					}

					TeethBrushingMessage.EVENING_BUTTON_ID -> {
						if (getCurrentTime().hour < LATE_REFRESH_HOUR) {
							event.interaction.respondEphemeral {
								content = "Please wait till after dinner before brushing your teeth again."
							}
						} else {
							teethBrushingMessage?.completeEvening()

							ConfigHandler.save()

							event.interaction.deferPublicMessageUpdate()
						}
					}

					CourseraMessage.COMPLETE_COURSE_BUTTON_ID -> {
						courseraMessage?.completeCourse()

						ConfigHandler.save()

						event.interaction.deferPublicMessageUpdate()
					}

					EstrogenMessage.COMPLETE_BUTTON_ID -> {
						if (medicineMessage?.hasEstrogen() != false) {
							estrogenMessage?.complete()
							medicineMessage?.useEstrogen()

							ConfigHandler.save()

							event.interaction.deferPublicMessageUpdate()
						} else {
							event.interaction.respondEphemeral { content = "You don't have any estrogen to take." }
						}
					}

					AntiAndrogenMessage.USE_LARGE_BUTTON_ID -> {
						if (medicineMessage?.hasCutAntiAndrogen() != false) {
							antiAndrogenMessage?.use(AntiAndrogenSize.LARGE)
							medicineMessage?.useAntiAndrogen()

							ConfigHandler.save()

							event.interaction.deferPublicMessageUpdate()
						} else {
							event.interaction.respondEphemeral { content = "You don't have any anti-androgen to take." }
						}
					}

					AntiAndrogenMessage.USE_MEDIUM_BUTTON_ID -> {
						if (medicineMessage?.hasCutAntiAndrogen() != false) {
							antiAndrogenMessage?.use(AntiAndrogenSize.MEDIUM)
							medicineMessage?.useAntiAndrogen()

							ConfigHandler.save()

							event.interaction.deferPublicMessageUpdate()
						} else {
							event.interaction.respondEphemeral { content = "You don't have any anti-androgen to take." }
						}
					}

					AntiAndrogenMessage.USE_SMALL_BUTTON_ID -> {
						if (medicineMessage?.hasCutAntiAndrogen() != false) {
							antiAndrogenMessage?.use(AntiAndrogenSize.SMALL)
							medicineMessage?.useAntiAndrogen()

							ConfigHandler.save()

							event.interaction.deferPublicMessageUpdate()
						} else {
							event.interaction.respondEphemeral { content = "You don't have any anti-androgen to take." }
						}
					}

					AntiAndrogenMessage.CUT_ANTI_ANDROGEN_BUTTON_ID -> {
						if (medicineMessage == null) {
							event.interaction.respondEphemeral { content = "Please create the medicine reminder first." }
						} else {
							medicineMessage!!.apply {
								if (hasWholeAntiAndrogen()) {
									cutAntiAndrogen()

									ConfigHandler.save()

									event.interaction.deferPublicMessageUpdate()
								} else {
									event.interaction.respondEphemeral { content = "You don't have any anti-androgen to cut." }
								}
							}
						}
					}

					MedicineMessage.RESTOCK_BUTTON_ID -> {
						val modal = MedicineRestockModal()

						modal.sendAndAwait(this) {
							if (it != null) {
								val estrogenPacks = modal.estrogenPacks.value?.toUIntOrNull()
								val wholeAntiAndrogens = modal.wholeAntiAndrogens.value?.toUIntOrNull()

								if (estrogenPacks == null) {
									it.respondEphemeral { content = "Cannot restock as amount specified for Estrogen Packs is not a positive whole number." }
								} else if (wholeAntiAndrogens == null) {
									it.respondEphemeral { content = "Cannot restock as amount specified for Anti-Androgens is not a positive whole number." }
								} else {
									medicineMessage?.restock(estrogenPacks, wholeAntiAndrogens)

									ConfigHandler.save()

									it.deferPublicMessageUpdate()
								}
							}
						}
					}
				}
			}
		}

		val reminders = ConfigHandler.get().reminders

		teethBrushingMessage = reminders.filterIsInstance<TeethBrushingReminder>().firstOrNull()?.let {
			TeethBrushingMessage(it, this)
		}

		courseraMessage = reminders.filterIsInstance<CourseraReminder>().firstOrNull()?.let {
			CourseraMessage(it, this)
		}

		estrogenMessage = reminders.filterIsInstance<EstrogenReminder>().firstOrNull()?.let {
			EstrogenMessage(it, this)
		}

		antiAndrogenMessage = reminders.filterIsInstance<AntiAndrogenReminder>().firstOrNull()?.let {
			AntiAndrogenMessage(it, this)
		}

		medicineMessage = reminders.filterIsInstance<MedicineReminder>().firstOrNull()?.let {
			MedicineMessage(it, this)
		}

		scheduleFixedReminderUpdates()
	}

	fun invalidateReminder(reminderData: Reminder) {
		when (reminderData) {
			is TeethBrushingReminder -> teethBrushingMessage = null
			is CourseraReminder -> courseraMessage = null
			is EstrogenReminder -> estrogenMessage = null
			is AntiAndrogenReminder -> antiAndrogenMessage = null
			is MedicineReminder -> medicineMessage = null
		}
	}

	private fun EphemeralSlashCommandContext<*, *>.refreshReminders() {
		executor.schedule(::updateReminders, 10, TimeUnit.SECONDS)
	}

	private fun scheduleFixedReminderUpdates() {
		val timeZone = ConfigHandler.get().misc.timezone
		val now = getCurrentTime()
		val isEarlyRefresh = ConfigHandler.get().misc.lastEarlyRefresh != now.date

		val earlyRefreshDelay = run {
			val earlyRefreshTime = LocalDateTime(now.date.plus(1, DateTimeUnit.DAY), LocalTime(EARLY_REFRESH_HOUR, 0, 0, 0))

			(earlyRefreshTime.toInstant(timeZone) - now.toInstant(timeZone)).inWholeSeconds
		}

		val lateRefreshDelay = run {
			val lateRefreshTime = if (now.hour < LATE_REFRESH_HOUR) {
				LocalDateTime(now.date, LocalTime(LATE_REFRESH_HOUR, 0, 0, 0))
			} else {
				LocalDateTime(now.date.plus(1, DateTimeUnit.DAY), LocalTime(LATE_REFRESH_HOUR, 0, 0, 0))
			}

			(lateRefreshTime.toInstant(timeZone) - now.toInstant(timeZone)).inWholeSeconds
		}

		if (isEarlyRefresh) {
			executor.schedule(::updateReminders, 10, TimeUnit.SECONDS)
		}

		executor.scheduleAtFixedRate(::updateReminders, earlyRefreshDelay, 1.days.inWholeSeconds, TimeUnit.SECONDS)
		executor.scheduleAtFixedRate(::updateReminders, lateRefreshDelay, 1.days.inWholeSeconds, TimeUnit.SECONDS)
	}

	private fun updateReminders() {
		val now = getCurrentTime()
		val config = ConfigHandler.get()
		val isEarlyRefresh = config.misc.lastEarlyRefresh != now.date

		if (isEarlyRefresh) {
			teethBrushingMessage?.reset()
			courseraMessage?.reset()
			estrogenMessage?.reset()
			antiAndrogenMessage?.reset()
			medicineMessage?.reset()

			config.misc.lastEarlyRefresh = now.date
		}

		runBlocking {
			teethBrushingMessage?.update()
			courseraMessage?.update()
			estrogenMessage?.update()
			antiAndrogenMessage?.update()
			medicineMessage?.update()
		}

		if (isEarlyRefresh) {
			ConfigHandler.save()
		}
	}
}
