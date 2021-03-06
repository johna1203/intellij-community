package com.jetbrains.python.edu.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.jetbrains.python.edu.StudyState;
import com.jetbrains.python.edu.course.Lesson;
import com.jetbrains.python.edu.course.Task;
import com.jetbrains.python.edu.course.TaskFile;
import com.jetbrains.python.edu.editor.StudyEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;


abstract public class StudyTaskNavigationAction extends DumbAwareAction {
  public void navigateTask(@NotNull final Project project) {
    StudyEditor studyEditor = StudyEditor.getSelectedStudyEditor(project);
    StudyState studyState = new StudyState(studyEditor);
    if (!studyState.isValid()) {
      return;
    }
    Task nextTask = getTargetTask(studyState.getTask());
    if (nextTask == null) {
      BalloonBuilder balloonBuilder =
        JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(getNavigationFinishedMessage(), MessageType.INFO, null);
      Balloon balloon = balloonBuilder.createBalloon();
      assert studyEditor != null;
      balloon.showInCenterOf(getButton(studyEditor));
      return;
    }
    for (VirtualFile file : FileEditorManager.getInstance(project).getOpenFiles()) {
      FileEditorManager.getInstance(project).closeFile(file);
    }
    int nextTaskIndex = nextTask.getIndex();
    int lessonIndex = nextTask.getLesson().getIndex();
    Map<String, TaskFile> nextTaskFiles = nextTask.getTaskFiles();
    if (nextTaskFiles.isEmpty()) {
      return;
    }
    VirtualFile projectDir = project.getBaseDir();
    String lessonDirName = Lesson.LESSON_DIR + String.valueOf(lessonIndex + 1);
    if (projectDir == null) {
      return;
    }
    VirtualFile lessonDir = projectDir.findChild(lessonDirName);
    if (lessonDir == null) {
      return;
    }
    String taskDirName = Task.TASK_DIR + String.valueOf(nextTaskIndex + 1);
    VirtualFile taskDir = lessonDir.findChild(taskDirName);
    if (taskDir == null) {
      return;
    }
    VirtualFile shouldBeActive = null;
    for (Map.Entry<String, TaskFile> entry : nextTaskFiles.entrySet()) {
      String name = entry.getKey();
      TaskFile taskFile = entry.getValue();
      VirtualFile vf = taskDir.findChild(name);
      if (vf != null) {
        FileEditorManager.getInstance(project).openFile(vf, true);
        if (!taskFile.getTaskWindows().isEmpty()) {
          shouldBeActive = vf;
        }
      }
    }
    if (shouldBeActive != null) {
      FileEditorManager.getInstance(project).openFile(shouldBeActive, true);
    }
    ToolWindow runToolWindow = ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.RUN);
    if (runToolWindow != null) {
      runToolWindow.hide(null);
    }
  }

  protected abstract JButton getButton(@NotNull final StudyEditor selectedStudyEditor);

  @Override
  public void actionPerformed(AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) {
      return;
    }
    navigateTask(project);
  }

  protected abstract String getNavigationFinishedMessage();

  protected abstract Task getTargetTask(@NotNull final Task sourceTask);
}
