# Undo for destructive task actions

## Goal

Match mainstream task apps (Todoist/TickTick): deleting a task or a completion
record shows the notice with an "撤销" action that undoes the operation before
it becomes permanent, removing mis-tap anxiety.

## What I already know

- Global notice is a fixed top overlay (TopNoticeHost in MainActivity.kt) fed
  by the single snackbarHostState; cards are already tap-to-dismiss.
- Deletion flows: `taskForDelete` dialog → `repository.deleteTask(task.id)`
  (hard Room delete); completion-record delete →
  `repository.deleteCompletion(completionId)` (hard delete) — both fire from
  MainActivity.
- `showSnackbar` returns a `SnackbarResult`; with `SnackbarDuration.Long` the
  caller can await the result and act on `ActionPerformed`.

## Requirements

- 删除事项:先软取消(从列表隐藏)→ 通知带「撤销」→ 时限内撤销则恢复,超时真正删除。
- 删除完成记录:同样带「撤销」。
- 通知文案不变;撤销 action 只出现在删除类通知上。
- 撤销期间数据一致(日历事件、提醒不产生副作用,或撤销时还原)。

## Acceptance Criteria

- [ ] 删除事项/完成记录后通知上出现「撤销」,点击即恢复原状。
- [ ] 超时(约 5s)后数据真正删除,行为与现在一致。
- [ ] 日历事件同步行为不被破坏(删除任务仍清理事件;撤销后事件保留)。

## Out of Scope

- 批量操作、归档撤销(归档本身可逆)。

## Technical Notes

- 删除实现改为"先保留实体,通知确认后删"或"延迟协程提交删除"。
- CalendarWriter.deleteEvent 的调用需随确认结果执行。
