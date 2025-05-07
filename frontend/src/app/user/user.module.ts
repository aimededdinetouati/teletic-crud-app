import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';

import { SharedModule } from '../shared/shared.module';
import { UserDashboardComponent } from './components/user-dashboard/user-dashboard.component';
import { UserTaskListComponent } from './components/user-task-list/user-task-list.component';
import { UserTaskDetailComponent } from './components/user-task-detail/user-task-detail.component';

const userRoutes: Routes = [
  { path: 'dashboard', component: UserDashboardComponent },
  { path: 'tasks', component: UserTaskListComponent },
  { path: 'tasks/:id', component: UserTaskDetailComponent },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
];

@NgModule({
  declarations: [

  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    SharedModule,
    RouterModule.forChild(userRoutes),
    UserDashboardComponent,
    UserTaskListComponent,
    UserTaskDetailComponent
  ]
})
export class UserModule { }