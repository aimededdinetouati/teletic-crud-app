import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';

import { HeaderComponent } from './components/header/header.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { MainLayoutComponent } from './components/main-layout/main-layout.component';
import { LoadingSpinnerComponent } from './components/loading-spinner/loading-spinner.component';
import { AlertComponent } from './components/alert/alert.component';
import { ConfirmDialogComponent } from './components/confirm-dialog/confirm-dialog.component';

@NgModule({
  declarations: [

  ],
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    HeaderComponent,
    SidebarComponent,
    MainLayoutComponent,
    LoadingSpinnerComponent,
    AlertComponent,
    ConfirmDialogComponent
  ],
  exports: [
    HeaderComponent,
    SidebarComponent,
    MainLayoutComponent,
    LoadingSpinnerComponent,
    AlertComponent,
    ConfirmDialogComponent,
    ReactiveFormsModule
  ]
})
export class SharedModule { }