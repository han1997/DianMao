# Swipe gestures on task rows

## Goal

Add mainstream row gestures — swipe right to complete, swipe left to delete —
to task rows in list contexts, coordinated with the existing long-press drag
reorder.

## What I already know

- TaskRow is a Card used in home smart lists, quadrant detail LazyColumn
  (with DragDropListState long-press reorder), and collapsed completed groups.
- Drag uses `detectDragGesturesAfterLongPress` on the LazyColumn — horizontal
  swipe on the row itself does not conflict with vertical drag, but must not
  fight the horizontal pager (none present).
- Deletion currently goes through the confirm dialog + undo (task 1).

## Requirements

- 右滑(向右)→ 标记完成(重复任务走既有确认流);左滑 → 删除(走确认+撤销)。
- 手势有背景提示(完成=象限色/删除=error),阈值触发,可中途取消。
- 与长按拖拽排序共存:拖拽仅长按触发,滑动为短距水平;互不误触。
- 已完成行右滑为"标为未完成"或禁用(实施时取更直觉者)。

## Acceptance Criteria

- [ ] 列表中左右滑触发完成/删除,带视觉反馈与阈值。
- [ ] 拖拽排序行为不回归;误触率低(阈值+速度判定)。
- [ ] 删除仍经过确认+撤销链路。

## Out of Scope

- 批量滑动选择、自定义手势映射。

## Technical Notes

- SwipeToDismissBox(M3 1.2)或自定义 anchoredDraggable;注意与 Card 圆角/边框的裁剪。
- 仅在列表上下文启用;详情页无行手势。
