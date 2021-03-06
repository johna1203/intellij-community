package com.jetbrains.python.refactoring.classes.extractSuperclass;

import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.classMembers.MemberInfoModel;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyElement;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyUtil;
import com.jetbrains.python.refactoring.classes.PyMemberInfoStorage;
import com.jetbrains.python.refactoring.classes.membersManager.PyMemberInfo;
import com.jetbrains.python.refactoring.classes.membersManager.vp.BadDataException;
import com.jetbrains.python.refactoring.classes.membersManager.vp.MembersBasedPresenterNoPreviewImpl;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Ilya.Kazakevich
 */
class PyExtractSuperclassPresenterImpl extends MembersBasedPresenterNoPreviewImpl<PyExtractSuperclassView,
  MemberInfoModel<PyElement, PyMemberInfo<PyElement>>>
  implements PyExtractSuperclassPresenter {
  private final NamesValidator myNamesValidator = LanguageNamesValidation.INSTANCE.forLanguage(PythonLanguage.getInstance());

  PyExtractSuperclassPresenterImpl(@NotNull final PyExtractSuperclassView view,
                                   @NotNull final PyClass classUnderRefactoring,
                                   @NotNull final PyMemberInfoStorage infoStorage) {
    super(view, classUnderRefactoring, infoStorage, new PyExtractSuperclassInfoModel(classUnderRefactoring));
  }

  @Override
  protected void validateView() throws BadDataException {
    super.validateView();
    final Project project = myClassUnderRefactoring.getProject();
    if (!myNamesValidator.isIdentifier(myView.getSuperClassName(), project)) {
      throw new BadDataException(PyBundle.message("refactoring.extract.super.name.0.must.be.ident", myView.getSuperClassName()));
    }
    boolean rootFound = false;
    final File moduleFile = new File(myView.getModuleFile());
    try {
      final String targetDir = FileUtil.toSystemIndependentName(moduleFile.getCanonicalPath());
      for (final VirtualFile file : ProjectRootManager.getInstance(project).getContentRoots()) {
        if (StringUtil.startsWithIgnoreCase(targetDir, file.getPath())) {
          rootFound = true;
          break;
        }
      }
    }
    catch (final IOException ignore) {
    }
    if (!rootFound) {
      throw new BadDataException(PyBundle.message("refactoring.extract.super.target.path.outside.roots"));
    }

    // TODO: Cover with test. It can't be done for now, because testFixture reports root path incorrectly
    // PY-12173
    myView.getModuleFile();
    final VirtualFile moduleVirtualFile = LocalFileSystem.getInstance().findFileByIoFile(moduleFile);
    if (moduleVirtualFile != null) {
      final PsiFile psiFile = PsiManager.getInstance(project).findFile(moduleVirtualFile);
      if (psiFile instanceof PyFile) {
        if (((PyFile)psiFile).findTopLevelClass(myView.getSuperClassName()) != null) {
          throw new BadDataException(PyBundle.message("refactoring.extract.super.target.class.already.exists", myView.getSuperClassName()));
        }
      }
    }
  }

  @Override
  public void launch() {
    final String defaultFilePath = FileUtil.toSystemDependentName(myClassUnderRefactoring.getContainingFile().getVirtualFile().getPath());
    final VirtualFile[] roots = ProjectRootManager.getInstance(myClassUnderRefactoring.getProject()).getContentRoots();
    final Collection<PyMemberInfo<PyElement>> pyMemberInfos =
      PyUtil.filterOutObject(myStorage.getClassMemberInfos(myClassUnderRefactoring));
    myView.configure(
      new PyExtractSuperclassInitializationInfo(myModel, pyMemberInfos, defaultFilePath,
                                                roots)
    );
    myView.initAndShow();
  }

  @NotNull
  @Override
  protected String getCommandName() {
    return RefactoringBundle.message("extract.superclass.command.name", myView.getSuperClassName(), myClassUnderRefactoring.getName());
  }

  @Override
  protected void refactorNoPreview() {
    PyExtractSuperclassHelper
      .extractSuperclass(myClassUnderRefactoring, myView.getSelectedMemberInfos(), myView.getSuperClassName(), myView.getModuleFile());
  }

  @NotNull
  @Override
  protected Iterable<? extends PyClass> getDestClassesToCheckConflicts() {
    return Collections.emptyList(); // No conflict can take place in newly created classes
  }
}
